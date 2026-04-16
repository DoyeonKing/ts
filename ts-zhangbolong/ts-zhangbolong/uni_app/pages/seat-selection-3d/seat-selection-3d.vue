<template>
	<view class="page">
		<view class="header-card">
			<text class="perf-name">{{ performanceName }}</text>
			<text class="perf-meta">{{ dateTime }} · {{ venue }}</text>
		</view>
		<!-- 小程序端：Three.js 3D 选座 -->
		<!-- #ifdef MP-WEIXIN -->
		<view class="canvas-wrap">
			<canvas type="webgl" id="three-canvas" canvas-id="three-canvas" class="three-canvas"
				@touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"
			></canvas>
		</view>
		<view class="canvas-hint">轻触座位→第一视角；俯瞰时拖拽旋转/双指缩放；第一视角时拖拽可左右上下环顾</view>
		<view class="seat-info" v-if="currentSeat">
			<text class="seat-text">当前座位：{{ currentSeat.row }}排{{ currentSeat.col }}座 · ¥{{ currentSeat.price }}</text>
			<view class="seat-info-btns">
				<button class="btn-back-view" @click="backToOverview">返回俯瞰</button>
				<button class="btn-seat-confirm" @click="confirmSeat">确认选座</button>
			</view>
		</view>
		<!-- #endif -->
		<!-- 非微信端：2D 备选 -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="view-tabs">
			<view class="tab active">3D 选座（请使用微信小程序）</view>
		</view>
		<view class="seat-view">
			<view class="stage-mini">舞台</view>
			<view class="seats-in-section">
				<view v-for="(seatId, i) in currentSectionSeats" :key="i"
					:class="['seat-3d', selectedSeats.indexOf(seatId) >= 0 ? 'selected' : 'available']"
					@click="toggleSeat(seatId)">
					<text>{{ seatId }}</text>
				</view>
			</view>
		</view>
		<!-- #endif -->
		<view class="legend">
			<view class="legend-item"><view class="dot available"></view><text>可选</text></view>
			<view class="legend-item"><view class="dot selected"></view><text>已选</text></view>
			<view class="legend-item"><view class="dot sold"></view><text>已售</text></view>
		</view>
		<view class="bottom-bar">
			<view class="selected-sum">已选 {{ selectedSeats.length }} 座 · {{ selectedSeats.join('、') || '暂无' }}</view>
			<view class="total">¥{{ totalPrice }}</view>
			<view class="btn-confirm" :class="{ disabled: selectedSeats.length === 0 }" @click="confirm">确认选座</view>
		</view>
	</view>
</template>

<script>
const PRICE_PER_SEAT = 180
const SOLD_SEATS = ['A1', 'A2', 'B5', 'C10', 'D15', 'E8']
const ROWS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']
const COLS = 12

function buildSeatList() {
	const list = []
	const soldSet = new Set(SOLD_SEATS)
	for (let r = 0; r < ROWS.length; r++) {
		for (let c = 0; c < COLS; c++) {
			const seatId = ROWS[r] + (c + 1)
			list.push({
				row: ROWS[r],
				col: c + 1,
				seatId,
				price: PRICE_PER_SEAT,
				status: soldSet.has(seatId) ? 1 : 0,
				x: (c - COLS / 2) * 1.1,
				y: 0.3,
				z: -2 - r * 1.0
			})
		}
	}
	return list
}

const SEAT_LIST = buildSeatList()
const STAGE_CENTER = { x: 0, y: 1, z: -8 }

export default {
	data() {
		return {
			performanceId: '',
			scheduleId: '',
			performanceName: '',
			dateTime: '',
			venue: '',
			currentSection: 'A区',
			sections: ['A区', 'B区', 'C区', 'D区', 'E区', 'VIP区'],
			selectedSeats: [],
			currentSeat: null,
			// Three.js（仅 MP-WEIXIN 使用）
			canvasRect: { left: 0, top: 0, width: 300, height: 400 },
			orbit: { theta: 0.4, phi: 0.5, dist: 22 },
			touchStart: { x: 0, y: 0 },
			touchStartDist: 0,
			touchStartDistVal: 22,
			isDragging: false,
			animId: null,
			// 第一视角下拖拽环顾：左右 yaw、上下 pitch（弧度）
			firstPersonYaw: 0,
			firstPersonPitch: 0
		}
	},
	computed: {
		currentSectionSeats() {
			const n = (this.sections.findIndex(s => s === this.currentSection) >= 0 ? 10 : 8)
			const prefix = (this.currentSection || 'A').replace('区', '')
			const list = []
			for (let i = 1; i <= n; i++) list.push(prefix + i)
			return list
		},
		totalPrice() {
			return this.selectedSeats.length * PRICE_PER_SEAT
		}
	},
	onLoad(opts) {
		this.performanceId = opts.performanceId || opts.id || '1'
		this.scheduleId = opts.scheduleId || '1'
		this.performanceName = opts.performanceName ? decodeURIComponent(opts.performanceName || '') : '《哈姆雷特》'
		this.dateTime = opts.dateTime ? decodeURIComponent(opts.dateTime || '') : '2024-03-01 19:30'
		this.venue = opts.venue ? decodeURIComponent(opts.venue || '') : '国家大剧院 歌剧厅'
	},
	onReady() {
		// #ifdef MP-WEIXIN
		this.initThree()
		// #endif
	},
	onUnload() {
		// #ifdef MP-WEIXIN
		if (this.animId != null && this.canvasNode && this.canvasNode.cancelAnimationFrame) {
			this.canvasNode.cancelAnimationFrame(this.animId)
		}
		this.animId = null
		if (this.renderer) {
			try { this.renderer.dispose() } catch (e) {}
			this.renderer = null
		}
		this.scene = null
		this.camera = null
		this.seatMeshes = []
		// #endif
	},
	methods: {
		// #ifdef MP-WEIXIN
		initThree() {
			const that = this
			uni.createSelectorQuery().in(this).select('#three-canvas').node().exec((res) => {
				if (!res || !res[0] || !res[0].node) {
					uni.showToast({ title: 'Canvas 未就绪', icon: 'none' })
					return
				}
				const canvas = res[0].node
				that.canvasNode = canvas
				uni.createSelectorQuery().in(that).select('#three-canvas').boundingClientRect((rect) => {
					if (rect) that.canvasRect = { left: rect.left, top: rect.top, width: rect.width, height: rect.height }
				}).exec()
				const sys = uni.getSystemInfoSync()
				const cw = that.canvasRect.width || sys.windowWidth || 300
				const ch = that.canvasRect.height || 420

				let createScopedThreejs
				try {
					const threeModule = require('threejs-miniprogram')
					createScopedThreejs = threeModule.createScopedThreejs || threeModule.default && threeModule.default.createScopedThreejs
				} catch (e) {
					console.error('threejs-miniprogram load error:', e)
					uni.showToast({ title: '请先构建 npm', icon: 'none' })
					return
				}
				if (!createScopedThreejs) {
					uni.showToast({ title: 'Three.js 适配库未就绪', icon: 'none' })
					return
				}

				const THREE = createScopedThreejs(canvas)
				that.THREE = THREE

				const scene = new THREE.Scene()
				scene.background = new THREE.Color(0.1, 0.1, 0.1)

				const camera = new THREE.PerspectiveCamera(45, cw / ch, 0.1, 100)
				camera.position.set(0, 8, 18)
				camera.lookAt(STAGE_CENTER.x, STAGE_CENTER.y, STAGE_CENTER.z)

				const renderer = new THREE.WebGLRenderer({ canvas, antialias: false })
				renderer.setSize(cw, ch)
				renderer.setPixelRatio(Math.min(uni.getSystemInfoSync().pixelRatio || 2, 2))

				// 环境光 + 平行光
				scene.add(new THREE.AmbientLight(0x666666))
				const dirLight = new THREE.DirectionalLight(0xffffff, 0.8)
				dirLight.position.set(5, 10, 5)
				scene.add(dirLight)

				// 舞台
				const stageGeom = new THREE.BoxGeometry(20, 0.5, 3)
				const stageMat = new THREE.MeshBasicMaterial({ color: 0x8B4513 })
				const stageMesh = new THREE.Mesh(stageGeom, stageMat)
				stageMesh.position.set(0, 0, -8)
				scene.add(stageMesh)

				// 座位
				const seatMeshes = []
				const soldSet = new Set(SOLD_SEATS)
				for (let i = 0; i < SEAT_LIST.length; i++) {
					const s = SEAT_LIST[i]
					const geom = new THREE.BoxGeometry(0.7, 0.5, 0.6)
					const mat = new THREE.MeshBasicMaterial({ color: 0x4a4a4a })
					const mesh = new THREE.Mesh(geom, mat)
					mesh.position.set(s.x, s.y, s.z)
					mesh.userData = { seatData: s }
					scene.add(mesh)
					seatMeshes.push(mesh)
				}

				that.scene = scene
				that.camera = camera
				that.renderer = renderer
				that.seatMeshes = seatMeshes
				that._updateSeatColors()
				that._animate()
			})
		},
		_animate() {
			if (!this.renderer || !this.scene || !this.camera) return
			if (!this.currentSeat) {
				const orbit = this.orbit
				this.camera.position.x = orbit.dist * Math.sin(orbit.phi) * Math.cos(orbit.theta)
				this.camera.position.y = orbit.dist * Math.cos(orbit.phi)
				this.camera.position.z = orbit.dist * Math.sin(orbit.phi) * Math.sin(orbit.theta)
				this.camera.lookAt(STAGE_CENTER.x, STAGE_CENTER.y, STAGE_CENTER.z)
			} else {
				// 第一视角：根据拖拽的 yaw/pitch 更新朝向，实现左右、上下环顾
				const yaw = this.firstPersonYaw
				const pitch = this.firstPersonPitch
				const P = this.camera.position
				let vx = STAGE_CENTER.x - P.x
				let vy = STAGE_CENTER.y - P.y
				let vz = STAGE_CENTER.z - P.z
				let len = Math.sqrt(vx * vx + vy * vy + vz * vz)
				if (len > 1e-5) {
					vx /= len
					vy /= len
					vz /= len
					const cy = Math.cos(yaw)
					const sy = Math.sin(yaw)
					const vx2 = vx * cy - vz * sy
					const vz2 = vx * sy + vz * cy
					vx = vx2
					vz = vz2
					const L = Math.sqrt(vx * vx + vz * vz)
					if (L > 1e-5) {
						const cp = Math.cos(pitch)
						const sp = Math.sin(pitch)
						const vy2 = vy * cp + L * sp
						const L2 = -vy * sp + L * cp
						const s = L2 / L
						vx *= s
						vz *= s
						vy = vy2
						len = Math.sqrt(vx * vx + vy * vy + vz * vz)
						if (len > 1e-5) {
							this.camera.lookAt(P.x + vx, P.y + vy, P.z + vz)
						}
					}
				}
			}
			this._updateSeatColors()
			this.renderer.render(this.scene, this.camera)
			const canvas = this.canvasNode
			if (canvas && canvas.requestAnimationFrame) {
				this.animId = canvas.requestAnimationFrame(() => this._animate())
			}
		},
		_updateSeatColors() {
			if (!this.seatMeshes || !this.seatMeshes.length) return
			const selected = this.selectedSeats || []
			this.seatMeshes.forEach((mesh) => {
				const s = mesh.userData && mesh.userData.seatData
				if (!mesh.material) return
				let color
				if (s && s.status === 1) color = 0x555555
				else if (s && selected.indexOf(s.seatId) >= 0) color = 0x6366f1
				else color = 0x4a4a4a
				mesh.material.color.setHex(color)
			})
		},
		onTouchStart(e) {
			if (!e.touches || !e.touches[0]) return
			this.touchStart.x = e.touches[0].clientX
			this.touchStart.y = e.touches[0].clientY
			this.isDragging = false
			if (e.touches.length >= 2) {
				const dx = e.touches[1].clientX - e.touches[0].clientX
				const dy = e.touches[1].clientY - e.touches[0].clientY
				this.touchStartDist = Math.sqrt(dx * dx + dy * dy)
				this.touchStartDistVal = this.orbit.dist
			}
		},
		onTouchMove(e) {
			if (!e.touches || !e.touches[0]) return
			const x = e.touches[0].clientX
			const y = e.touches[0].clientY
			if (Math.abs(x - this.touchStart.x) > 8 || Math.abs(y - this.touchStart.y) > 8) this.isDragging = true

			if (e.touches.length >= 2) {
				const dx = e.touches[1].clientX - e.touches[0].clientX
				const dy = e.touches[1].clientY - e.touches[0].clientY
				const dist = Math.sqrt(dx * dx + dy * dy)
				if (this.touchStartDist > 0) {
					const scale = dist / this.touchStartDist
					this.orbit.dist = Math.max(8, Math.min(35, this.touchStartDistVal * scale))
				}
			} else if (this.currentSeat) {
				// 第一视角：单指拖拽为左右/上下环顾
				this.firstPersonYaw += (x - this.touchStart.x) * 0.008
				this.firstPersonPitch += (y - this.touchStart.y) * 0.008
				this.firstPersonPitch = Math.max(-0.6, Math.min(0.6, this.firstPersonPitch))
				this.touchStart.x = x
				this.touchStart.y = y
			} else {
				this.orbit.theta += (x - this.touchStart.x) * 0.01
				this.orbit.phi = Math.max(0.2, Math.min(1.2, this.orbit.phi + (y - this.touchStart.y) * 0.01))
				this.touchStart.x = x
				this.touchStart.y = y
			}
		},
		onTouchEnd(e) {
			if (e.touches && e.touches.length >= 2) return
			if (!this.isDragging && e.changedTouches && e.changedTouches[0]) this._checkSeatClick(e)
		},
		_checkSeatClick(e) {
			if (!this.camera || !this.scene || !this.seatMeshes || !this.seatMeshes.length || !this.THREE) return
			const touch = e.changedTouches[0]
			const that = this
			// 每次点击时重新取 canvas 位置，避免坐标偏差
			uni.createSelectorQuery().in(this).select('#three-canvas').boundingClientRect((rect) => {
				if (!rect || !that.camera || !that.seatMeshes.length) return
				const w = rect.width || 300
				const h = rect.height || 400
				const nx = (touch.clientX - rect.left) / w * 2 - 1
				const ny = -((touch.clientY - rect.top) / h) * 2 + 1
				const THREE = that.THREE
				const raycaster = new THREE.Raycaster()
				const pointer = new THREE.Vector2(nx, ny)
				raycaster.setFromCamera(pointer, that.camera)
				const intersects = raycaster.intersectObjects(that.seatMeshes)
				for (let i = 0; i < intersects.length; i++) {
					const obj = intersects[i].object
					const seat = obj.userData && obj.userData.seatData
					if (seat && seat.status !== 1) {
						that._switchToSeatView(seat)
						return
					}
				}
			}).exec()
		},
		_switchToSeatView(seat) {
			this.currentSeat = seat
			this.firstPersonYaw = 0
			this.firstPersonPitch = 0
			if (!this.camera) return
			this.camera.position.set(seat.x, seat.y + 1.2, seat.z)
			this.camera.lookAt(STAGE_CENTER.x, STAGE_CENTER.y, STAGE_CENTER.z)
			uni.showToast({ title: `已切换到${seat.row}排${seat.col}座视角，可拖拽环顾`, icon: 'none' })
		},
		backToOverview() {
			this.currentSeat = null
			if (!this.camera) return
			const o = this.orbit
			this.camera.position.x = o.dist * Math.sin(o.phi) * Math.cos(o.theta)
			this.camera.position.y = o.dist * Math.cos(o.phi)
			this.camera.position.z = o.dist * Math.sin(o.phi) * Math.sin(o.theta)
			this.camera.lookAt(STAGE_CENTER.x, STAGE_CENTER.y, STAGE_CENTER.z)
			uni.showToast({ title: '已返回俯瞰视角', icon: 'none' })
		},
		confirmSeat() {
			if (!this.currentSeat) return
			const seatId = this.currentSeat.seatId
			if (this.selectedSeats.indexOf(seatId) >= 0) return
			this.selectedSeats = [...this.selectedSeats, seatId]
			this.currentSeat = null
		},
		// #endif
		toggleSeat(seatId) {
			const i = this.selectedSeats.indexOf(seatId)
			if (i >= 0) this.selectedSeats = this.selectedSeats.filter(s => s !== seatId)
			else this.selectedSeats = [...this.selectedSeats, seatId]
		},
		confirm() {
			if (this.selectedSeats.length === 0) {
				uni.showToast({ title: '请先选择座位', icon: 'none' })
				return
			}
			const seats = this.selectedSeats.join(',')
			const amount = this.selectedSeats.length * PRICE_PER_SEAT
			uni.navigateTo({
				url: `/pages/payment/payment?performanceId=${this.performanceId}&scheduleId=${this.scheduleId}&seats=${encodeURIComponent(seats)}&amount=${amount}&performanceName=${encodeURIComponent(this.performanceName)}&dateTime=${encodeURIComponent(this.dateTime)}`
			})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 180rpx; min-height: 100vh; background: #f5f5f5; }
.header-card { background: rgba(99,102,241,0.12); border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.perf-name { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 8rpx; }
.perf-meta { font-size: 26rpx; color: #666; display: block; }
.canvas-wrap { width: 100%; height: 420px; margin-bottom: 24rpx; border-radius: 16rpx; overflow: hidden; background: #1a1a1a; }
.three-canvas { width: 100%; height: 100%; display: block; }
.canvas-hint { font-size: 24rpx; color: #666; margin-top: 8rpx; margin-bottom: 16rpx; }
.seat-info { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.seat-text { font-size: 28rpx; color: #333; display: block; margin-bottom: 16rpx; }
.seat-info-btns { display: flex; gap: 16rpx; align-items: center; }
.btn-back-view { background: #e5e7eb; color: #374151; font-size: 26rpx; padding: 16rpx 32rpx; border-radius: 12rpx; }
.btn-seat-confirm { background: #6366f1; color: #fff; font-size: 26rpx; padding: 16rpx 32rpx; border-radius: 12rpx; }
.view-tabs { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.tab { flex: 1; text-align: center; padding: 20rpx; background: #fff; border-radius: 16rpx; font-size: 26rpx; color: #666; }
.tab.active { background: #6366f1; color: #fff; font-weight: 500; }
.seat-view { background: #1a1a1a; border-radius: 20rpx; padding: 32rpx; min-height: 360rpx; }
.stage-mini { background: #8B4513; color: #fff; text-align: center; padding: 12rpx; border-radius: 8rpx; margin-bottom: 24rpx; font-size: 24rpx; font-weight: bold; }
.seats-in-section { display: flex; flex-wrap: wrap; gap: 16rpx; justify-content: center; }
.seat-3d { width: 72rpx; height: 72rpx; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; font-size: 22rpx; color: #fff; }
.seat-3d.available { background: #444; border: 2rpx solid #555; }
.seat-3d.selected { background: #22c55e; border: 2rpx solid #16a34a; }
.legend { display: flex; gap: 32rpx; margin-top: 24rpx; }
.legend-item { display: flex; align-items: center; gap: 12rpx; font-size: 24rpx; color: #666; }
.dot { width: 28rpx; height: 28rpx; border-radius: 6rpx; }
.dot.available { background: #4a4a4a; }
.dot.selected { background: #6366f1; }
.dot.sold { background: #555; }
.bottom-bar { position: fixed; left: 0; right: 0; bottom: 0; display: flex; align-items: center; padding: 24rpx 32rpx; background: #fff; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.08); gap: 24rpx; }
.selected-sum { flex: 1; font-size: 24rpx; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.total { font-size: 32rpx; font-weight: bold; color: #6366f1; }
.btn-confirm { background: #6366f1; color: #fff; padding: 24rpx 40rpx; border-radius: 24rpx; font-size: 28rpx; }
.btn-confirm.disabled { background: #ccc; }
</style>
