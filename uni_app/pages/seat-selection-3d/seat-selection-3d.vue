<template>
	<view class="page">
		<view class="header-card">
			<view>
				<text class="eyebrow">IMMERSIVE THEATRE VIEW</text>
				<text class="perf-name">{{ performanceName }}</text>
				<text class="perf-meta">{{ dateTime }} · {{ venue }}</text>
				<text class="perf-note">仅用于选座参考与剧场视角展示，不提供支付</text>
			</view>
			<view class="badge-card">
				<text class="badge-value">{{ selectedSeats.length }}</text>
				<text class="badge-label">已选</text>
			</view>
		</view>
		<!-- 小程序端：Three.js 3D 选座 -->
		<!-- #ifdef MP-WEIXIN -->
		<view class="control-card">
			<text class="control-title">沉浸式观演模拟</text>
			<text class="control-subtitle">轻触座位进入观众席视角，可模拟前方有人时的遮挡效果</text>
			<view class="mode-row">
				<view
					v-for="mode in audienceModes"
					:key="mode.key"
					:class="['mode-chip', viewMode === mode.key ? 'active' : '']"
					@click="setViewMode(mode.key)"
				>
					{{ mode.label }}
				</view>
			</view>
			<text class="mode-desc">{{ currentModeDescription }}</text>
		</view>
		<view class="canvas-wrap">
			<view class="stage-tag">PROSCENIUM STAGE</view>
			<canvas v-if="threeReady && !renderFailed" type="webgl" id="three-canvas" canvas-id="three-canvas" class="three-canvas"
				@touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"
			></canvas>
			<view v-else class="fallback-wrap">
				<view class="fallback-stage">舞台</view>
				<view class="fallback-seat-map">
					<view v-for="row in fallbackSeatRows" :key="row.row" class="fallback-row" :style="{ paddingLeft: (row.rowIndex * 16) + 'rpx', paddingRight: (row.rowIndex * 16) + 'rpx' }">
						<text class="fallback-row-label">{{ row.row }}</text>
						<view class="fallback-row-seats">
							<view
								v-for="seat in row.seats"
								:key="seat.seatId"
								:class="['fallback-seat', seat.status === 1 ? 'sold' : selectedSeats.indexOf(seat.seatId) >= 0 ? 'selected' : 'available']"
								@click="previewSeatFromFallback(seat)"
							>
								{{ seat.col }}
							</view>
						</view>
					</view>
				</view>
				<text class="fallback-tip">3D 渲染未正常显示，先用保底剧场图点座位；渲染恢复后仍可进入第一视角。</text>
			</view>
		</view>
		<view class="canvas-hint">进入页面默认面向舞台；俯瞰时单指左右拖拽环绕剧场、双指缩放；第一视角时拖拽可左右上下环顾，并切换无遮挡 / 前方坐姿 / 前方高个</view>
		<view class="seat-info" v-if="currentSeat">
			<text class="seat-text">当前座位：{{ currentSeat.row }}排{{ currentSeat.col }}座</text>
			<text class="seat-summary">{{ seatViewSummary }}</text>
			<view class="seat-metrics">
				<view class="metric-card">
					<text class="metric-label">观演模式</text>
					<text class="metric-value">{{ viewModeLabel }}</text>
				</view>
				<view class="metric-card">
					<text class="metric-label">推荐指数</text>
					<text class="metric-value">{{ viewScore }}</text>
				</view>
			</view>
			<view class="seat-info-btns">
				<button class="btn-back-view" @click="backToOverview">返回俯瞰</button>
				<button class="btn-seat-confirm" @click="confirmSeat">加入参考座位</button>
			</view>
		</view>
		<!-- #endif -->
		<!-- 非微信端：2D 备选 -->
		<!-- #ifndef MP-WEIXIN -->
		<view class="view-tabs">
			<view class="tab active">3D 选座（请使用微信小程序体验剧场视角）</view>
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
		<view class="legend legend-card">
			<view class="legend-item"><view class="dot available"></view><text>可选</text></view>
			<view class="legend-item"><view class="dot selected"></view><text>已加入参考</text></view>
			<view class="legend-item"><view class="dot sold"></view><text>不可选</text></view>
		</view>
		<view class="bottom-bar">
			<view class="selected-sum">参考座位 {{ selectedSeats.length }} 个 · {{ selectedSeats.join('、') || '暂无' }}</view>
			<view class="btn-confirm" :class="{ disabled: selectedSeats.length === 0 }" @click="confirm">完成参考</view>
		</view>
	</view>
</template>

<script>
function safeDecode(value) {
	if (!value) return ''
	try {
		return decodeURIComponent(value)
	} catch (e) {
		return value
	}
}

const SOLD_SEATS = ['A1', 'A2', 'B5', 'C10', 'D15', 'E8']
const ROWS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']
const COLS = 12
const STAGE_CENTER = { x: 0, y: 3.5, z: -18.5 }
const VIEW_MODES = {
	clear: { label: '无遮挡', desc: '模拟空场状态，适合观察舞台整体开阔度。', pitch: 0.12, height: 0.06, delta: 0 },
	seated: { label: '前方坐姿', desc: '前排观众正常落座，会有轻微头部遮挡。', pitch: 0.14, height: 0, delta: -8 },
	tall: { label: '前方高个', desc: '前方有高个观众，舞台下沿与地面区域遮挡更明显。', pitch: 0.18, height: -0.04, delta: -18 }
}

function clamp(num, min, max) {
	return Math.max(min, Math.min(max, num))
}

function buildSeatList() {
	const list = []
	const soldSet = new Set(SOLD_SEATS)
	for (let r = 0; r < ROWS.length; r++) {
		for (let c = 0; c < COLS; c++) {
			const offset = c - (COLS - 1) / 2
			const curve = Math.abs(offset) * 0.16 * (1 + r * 0.03)
			const seatId = ROWS[r] + (c + 1)
			const rakeHeight = r * 0.32
			list.push({
				row: ROWS[r],
				rowIndex: r,
				col: c + 1,
				seatId,
				status: soldSet.has(seatId) ? 1 : 0,
				x: offset * 1.28,
				y: 0.18 + rakeHeight,
				z: -9.6 + r * 1.52 + curve
			})
		}
	}
	return list
}

const SEAT_LIST = buildSeatList()

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
			viewMode: 'clear',
			audienceModes: [
				{ key: 'clear', label: '无遮挡' },
				{ key: 'seated', label: '前方坐姿' },
				{ key: 'tall', label: '前方高个' }
			],
			canvasRect: { left: 0, top: 0, width: 300, height: 400 },
			orbit: { theta: 1.57, phi: 0.98, dist: 26 },
			touchStart: { x: 0, y: 0 },
			touchStartDist: 0,
			touchStartDistVal: 24,
			isDragging: false,
			animId: null,
			firstPersonYaw: 0,
			firstPersonPitch: 0,
			seatMeshes: [],
			audienceMeshes: [],
			threeReady: true,
			renderFailed: false
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
		currentModeDescription() {
			return VIEW_MODES[this.viewMode].desc
		},
		viewModeLabel() {
			return VIEW_MODES[this.viewMode].label
		},
		scoreNum() {
			if (!this.currentSeat) return 0
			const rowScore = 100 - this.currentSeat.rowIndex * 5
			const sidePenalty = Math.abs(this.currentSeat.col - (COLS + 1) / 2) * 3
			return clamp(Math.round(rowScore - sidePenalty + VIEW_MODES[this.viewMode].delta), 52, 98)
		},
		seatViewSummary() {
			if (!this.currentSeat) return ''
			const score = this.scoreNum
			if (score >= 88) return '舞台主体非常集中，人物调度与表情细节兼顾得较好。'
			if (score >= 76) return '视野整体均衡，适合看走位、灯光和舞台纵深。'
			if (score >= 66) return '可完整观看舞台，但边缘区域和地面细节会稍弱。'
			return '更适合体验现场氛围，建议结合无遮挡模式一起判断。'
		},
		viewScore() {
			return this.currentSeat ? this.scoreNum + ' / 100' : '--'
		},
		fallbackSeatRows() {
			const grouped = {}
			SEAT_LIST.forEach((seat) => {
				if (!grouped[seat.row]) grouped[seat.row] = []
				grouped[seat.row].push(seat)
			})
			return ROWS.map((row) => {
				const seats = (grouped[row] || []).sort((a, b) => a.col - b.col)
				return {
					row,
					rowIndex: seats[0] ? seats[0].rowIndex : 0,
					seats
				}
			})
		}
	},
	onLoad(opts) {
		this.performanceId = opts.performanceId || opts.id || '1'
		this.scheduleId = opts.scheduleId || '1'
		this.performanceName = safeDecode(opts.performanceName) || '《哈姆雷特》'
		this.dateTime = safeDecode(opts.dateTime) || '2024-03-01 19:30'
		this.venue = safeDecode(opts.venue) || '国家大剧院 歌剧厅'
	},
	onReady() {
		// #ifdef MP-WEIXIN
		this.threeReady = true
		this.renderFailed = false
		this.initThree()
		setTimeout(() => {
			if (!this.renderer || !this.scene || !this.camera) {
				this.renderFailed = true
			} else {
				this._renderScene()
			}
		}, 1200)
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
		this.audienceMeshes = []
		// #endif
	},
	methods: {
		setViewMode(modeKey) {
			this.viewMode = modeKey
			this._applyAudienceVisibility()
			if (this.currentSeat) this._switchToSeatView(this.currentSeat, true)
			else this._renderScene()
		},
		// #ifdef MP-WEIXIN
		initThree() {
			if (this.renderFailed) return
			const that = this
			uni.createSelectorQuery().in(this).select('#three-canvas').node().exec((res) => {
				if (!res || !res[0] || !res[0].node) {
					uni.showToast({ title: 'Canvas 未就绪', icon: 'none' })
					return
				}
				const canvas = res[0].node
				that.canvasNode = canvas
				that.threeReady = true
				that.renderFailed = false
				uni.createSelectorQuery().in(that).select('#three-canvas').boundingClientRect((rect) => {
					if (rect) {
						that.canvasRect = {
							left: rect.left || 0,
							top: rect.top || 0,
							width: rect.width || 300,
							height: rect.height || 500
						}
					}
				}).exec()
				let createScopedThreejs
				try {
					const threeModule = require('threejs-miniprogram')
					createScopedThreejs = threeModule.createScopedThreejs || threeModule.default && threeModule.default.createScopedThreejs
				} catch (e) {
					that.renderFailed = true
					uni.showToast({ title: '请先构建 npm', icon: 'none' })
					return
				}
				if (!createScopedThreejs) {
					that.renderFailed = true
					uni.showToast({ title: 'Three.js 适配库未就绪', icon: 'none' })
					return
				}
				const sys = uni.getSystemInfoSync()
				const cw = that.canvasRect.width || sys.windowWidth || 300
				const ch = that.canvasRect.height || 500
				const THREE = createScopedThreejs(canvas)
				that.THREE = THREE
				const scene = new THREE.Scene()
				scene.fog = new THREE.Fog(0x060814, 24, 46)
				scene.background = new THREE.Color(0x060814)

				const camera = new THREE.PerspectiveCamera(52, cw / ch, 0.1, 120)
				camera.position.set(0, 11, 15)
				camera.lookAt(STAGE_CENTER.x, STAGE_CENTER.y, STAGE_CENTER.z)

				const renderer = new THREE.WebGLRenderer({ canvas, antialias: false })
				renderer.setSize(cw, ch)
				renderer.setPixelRatio(Math.min(sys.pixelRatio || 2, 2))

				scene.add(new THREE.AmbientLight(0x7c6f8f, 0.8))
				const spot = new THREE.SpotLight(0xffe7b8, 1.8, 90, Math.PI / 6, 0.45)
				spot.position.set(0, 16, -9)
				spot.target.position.set(0, 1.5, -17)
				scene.add(spot)
				scene.add(spot.target)
				const fill = new THREE.PointLight(0x5973ff, 0.65, 60)
				fill.position.set(0, 8, 6)
				scene.add(fill)

				that._buildTheatre(scene, THREE)
				const seatMeshes = []
				SEAT_LIST.forEach((seat) => {
					const chair = that._createSeatMesh(THREE, seat)
					scene.add(chair)
					seatMeshes.push(chair)
				})

				that.scene = scene
				that.camera = camera
				that.renderer = renderer
				that.seatMeshes = seatMeshes
				that._buildAudienceModels()
				that._updateSeatColors()
				that._renderScene()
			})
		},
		_buildTheatre(scene, THREE) {
			const addMesh = (geometry, material, position, rotation) => {
				const mesh = new THREE.Mesh(geometry, material)
				mesh.position.set(position[0], position[1], position[2])
				if (rotation) mesh.rotation.set(rotation[0], rotation[1], rotation[2])
				scene.add(mesh)
			}
			addMesh(new THREE.PlaneGeometry(34, 40), new THREE.MeshPhongMaterial({ color: 0x151923, shininess: 18 }), [0, -0.02, -10], [-Math.PI / 2, 0, 0])
			addMesh(new THREE.PlaneGeometry(4.2, 24), new THREE.MeshPhongMaterial({ color: 0x52111e, shininess: 60 }), [0, 0.01, -8], [-Math.PI / 2, 0, 0])
			addMesh(new THREE.BoxGeometry(21, 1, 6), new THREE.MeshPhongMaterial({ color: 0x5b331f, shininess: 35 }), [0, 0.5, -18.5])
			addMesh(new THREE.BoxGeometry(20.6, 0.18, 5.6), new THREE.MeshPhongMaterial({ color: 0x875b35, shininess: 48 }), [0, 1.02, -18.5])
			addMesh(new THREE.BoxGeometry(22.5, 10.5, 1.2), new THREE.MeshPhongMaterial({ color: 0x191624 }), [0, 5.2, -21.6])
			addMesh(new THREE.BoxGeometry(22.4, 1.2, 1.2), new THREE.MeshPhongMaterial({ color: 0x7f1828 }), [0, 9.4, -15.7])
			addMesh(new THREE.BoxGeometry(1.4, 9.2, 1.2), new THREE.MeshPhongMaterial({ color: 0x7f1828 }), [-10.6, 5.1, -15.7])
			addMesh(new THREE.BoxGeometry(1.4, 9.2, 1.2), new THREE.MeshPhongMaterial({ color: 0x7f1828 }), [10.6, 5.1, -15.7])
			for (let i = 0; i < 7; i++) {
				addMesh(new THREE.BoxGeometry(2.2, 7.6 + (i % 2) * 0.3, 0.3), new THREE.MeshPhongMaterial({ color: 0x5e0f1a }), [-6.6 + i * 2.2, 4.6, -15.1 - (i % 2) * 0.1])
			}
			for (let i = 0; i < 10; i++) {
				const z = 2 - i * 2.25
				addMesh(new THREE.CylinderGeometry(0.08, 0.08, 0.18, 10), new THREE.MeshBasicMaterial({ color: 0xffbf66 }), [-2.25, 0.12, z])
				addMesh(new THREE.CylinderGeometry(0.08, 0.08, 0.18, 10), new THREE.MeshBasicMaterial({ color: 0xffbf66 }), [2.25, 0.12, z])
			}
		},
		_createSeatMesh(THREE, seat) {
			const group = new THREE.Group()
			const seatColor = seat.status === 1 ? 0x57606d : 0x8f152a
			const fabricMat = new THREE.MeshPhongMaterial({ color: seatColor, shininess: 28 })
			const frameMat = new THREE.MeshPhongMaterial({ color: 0x2b303a, shininess: 32 })
			const base = new THREE.Mesh(new THREE.BoxGeometry(0.82, 0.18, 0.82), fabricMat)
			base.position.set(0, 0.34, 0)
			group.add(base)
			const back = new THREE.Mesh(new THREE.BoxGeometry(0.82, 0.92, 0.18), fabricMat)
			back.position.set(0, 0.82, 0.29)
			group.add(back)
			const leftArm = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.46, 0.72), frameMat)
			leftArm.position.set(-0.42, 0.42, 0.02)
			group.add(leftArm)
			const rightArm = leftArm.clone()
			rightArm.position.x = 0.42
			group.add(rightArm)
			group.position.set(seat.x, seat.y, seat.z)
			group.userData = { seatData: seat, base, back, leftArm, rightArm }
			return group
		},
		_buildAudienceModels() {
			if (!this.THREE || !this.scene) return
			const THREE = this.THREE
			const bodyMat = new THREE.MeshPhongMaterial({ color: 0x1d2433, transparent: true, opacity: 0.88 })
			const headMat = new THREE.MeshPhongMaterial({ color: 0xe6c2a8, transparent: true, opacity: 0.9 })
			this.audienceMeshes = []
			SEAT_LIST.forEach((seat) => {
				if (seat.rowIndex >= ROWS.length - 1 || seat.status === 1) return
				const group = new THREE.Group()
				const body = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.24, 0.62, 10), bodyMat)
				body.position.set(0, 0.52, 0.08)
				group.add(body)
				const head = new THREE.Mesh(new THREE.SphereGeometry(0.16, 12, 12), headMat)
				head.position.set(0, 0.96, 0.14)
				group.add(head)
				group.position.set(seat.x, seat.y + 0.02, seat.z - 1.34)
				group.visible = false
				this.scene.add(group)
				this.audienceMeshes.push(group)
			})
			this._applyAudienceVisibility()
		},
		_applyAudienceVisibility() {
			(this.audienceMeshes || []).forEach((group) => {
				group.visible = this.viewMode !== 'clear'
				const factor = this.viewMode === 'tall' ? 1.14 : 1
				if (group.scale) group.scale.set(1, factor, 1)
			})
		},
		_animate() {
			this._updateSeatColors()
			this._renderScene()
		},
		_updateSeatColors() {
			if (!this.seatMeshes || !this.seatMeshes.length) return
			const selected = this.selectedSeats || []
			this.seatMeshes.forEach((mesh) => {
				const seat = mesh.userData && mesh.userData.seatData
				const base = mesh.userData && mesh.userData.base
				const back = mesh.userData && mesh.userData.back
				const leftArm = mesh.userData && mesh.userData.leftArm
				const rightArm = mesh.userData && mesh.userData.rightArm
				if (!seat || !base || !back) return
				const isCurrentSeat = this.currentSeat && this.currentSeat.seatId === seat.seatId
				mesh.visible = !isCurrentSeat
				if (leftArm) leftArm.visible = !isCurrentSeat
				if (rightArm) rightArm.visible = !isCurrentSeat
				let color = 0x8f152a
				if (seat.status === 1) color = 0x57606d
				else if (selected.indexOf(seat.seatId) >= 0) color = 0xd8a94f
				base.material.color.setHex(color)
				back.material.color.setHex(color)
			})
		},
		_renderScene() {
			if (!this.renderer || !this.scene || !this.camera) return
			if (!this.currentSeat) {
				const orbit = this.orbit
				const horizontalRadius = orbit.dist * Math.sin(orbit.phi)
				this.camera.position.x = STAGE_CENTER.x + horizontalRadius * Math.cos(orbit.theta)
				this.camera.position.y = 2 + orbit.dist * Math.cos(orbit.phi)
				this.camera.position.z = 1 + horizontalRadius * Math.sin(orbit.theta)
				this.camera.lookAt(STAGE_CENTER.x, STAGE_CENTER.y, STAGE_CENTER.z)
			} else {
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
						if (len > 1e-5) this.camera.lookAt(P.x + vx, P.y + vy, P.z + vz)
					}
				}
			}
			this.renderer.render(this.scene, this.camera)
		},
		onTouchStart(e) {
			if (!e.touches || !e.touches[0]) return
			const touch = e.touches[0]
			this.touchStart.x = touch.x != null ? touch.x : touch.clientX
			this.touchStart.y = touch.y != null ? touch.y : touch.clientY
			this.isDragging = false
			if (e.touches.length >= 2) {
				const x1 = e.touches[0].x != null ? e.touches[0].x : e.touches[0].clientX
				const y1 = e.touches[0].y != null ? e.touches[0].y : e.touches[0].clientY
				const x2 = e.touches[1].x != null ? e.touches[1].x : e.touches[1].clientX
				const y2 = e.touches[1].y != null ? e.touches[1].y : e.touches[1].clientY
				const dx = x2 - x1
				const dy = y2 - y1
				this.touchStartDist = Math.sqrt(dx * dx + dy * dy)
				this.touchStartDistVal = this.orbit.dist
			}
		},
		onTouchMove(e) {
			if (!e.touches || !e.touches[0]) return
			const x = e.touches[0].x != null ? e.touches[0].x : e.touches[0].clientX
			const y = e.touches[0].y != null ? e.touches[0].y : e.touches[0].clientY
			if (Math.abs(x - this.touchStart.x) > 8 || Math.abs(y - this.touchStart.y) > 8) this.isDragging = true
			let changed = false
			if (e.touches.length >= 2) {
				const x1 = e.touches[0].x != null ? e.touches[0].x : e.touches[0].clientX
				const y1 = e.touches[0].y != null ? e.touches[0].y : e.touches[0].clientY
				const x2 = e.touches[1].x != null ? e.touches[1].x : e.touches[1].clientX
				const y2 = e.touches[1].y != null ? e.touches[1].y : e.touches[1].clientY
				const dx = x2 - x1
				const dy = y2 - y1
				const dist = Math.sqrt(dx * dx + dy * dy)
				if (this.touchStartDist > 0) {
					this.orbit.dist = clamp(this.touchStartDistVal * (dist / this.touchStartDist), 10, 34)
					changed = true
				}
			} else if (this.currentSeat) {
				this.firstPersonYaw += (x - this.touchStart.x) * 0.008
				this.firstPersonPitch = clamp(this.firstPersonPitch + (y - this.touchStart.y) * 0.008, -0.62, 0.58)
				changed = true
			} else {
				this.orbit.theta += (x - this.touchStart.x) * 0.008
				changed = true
			}
			this.touchStart.x = x
			this.touchStart.y = y
			if (changed) this._renderScene()
		},
		onTouchEnd(e) {
			if (e.touches && e.touches.length >= 2) return
			if (!this.isDragging && e.changedTouches && e.changedTouches[0]) this._checkSeatClick(e)
		},
		_checkSeatClick(e) {
			if (!this.camera || !this.scene || !this.seatMeshes || !this.seatMeshes.length || !this.THREE) return
			const touch = e.changedTouches[0]
			const touchX = touch.x != null ? touch.x : touch.clientX
			const touchY = touch.y != null ? touch.y : touch.clientY
			const rect = this.canvasRect || { left: 0, top: 0, width: 300, height: 400 }
			const w = rect.width || 300
			const h = rect.height || 400
			const localX = touchX <= w ? touchX : touchX - (rect.left || 0)
			const localY = touchY <= h ? touchY : touchY - (rect.top || 0)
			const nx = localX / w * 2 - 1
			const ny = -(localY / h) * 2 + 1
			const raycaster = new this.THREE.Raycaster()
			raycaster.setFromCamera(new this.THREE.Vector2(nx, ny), this.camera)
			const intersects = raycaster.intersectObjects(this.seatMeshes, true)
			for (let i = 0; i < intersects.length; i++) {
				let obj = intersects[i].object
				while (obj && !(obj.userData && obj.userData.seatData) && obj.parent) obj = obj.parent
				const seat = obj && obj.userData && obj.userData.seatData
				if (seat && seat.status !== 1) {
					this._switchToSeatView(seat)
					return
				}
			}
			uni.showToast({ title: '请点中具体座位查看第一视角', icon: 'none' })
		},
		previewSeatFromFallback(seat) {
			if (!seat || seat.status === 1) {
				uni.showToast({ title: '该座位不可选', icon: 'none' })
				return
			}
			this._switchToSeatView(seat)
		},
		_switchToSeatView(seat, silent) {
			this.currentSeat = seat
			this.firstPersonYaw = 0
			this.firstPersonPitch = VIEW_MODES[this.viewMode].pitch
			if (!this.camera) {
				if (!silent) uni.showToast({ title: `已切换到${seat.row}排${seat.col}座参考视角`, icon: 'none' })
				return
			}
			const eyeY = seat.y + 1.24 + VIEW_MODES[this.viewMode].height
			const eyeZ = seat.z + 0.24
			this.camera.position.set(seat.x, eyeY, eyeZ)
			this.camera.lookAt(seat.x, eyeY + VIEW_MODES[this.viewMode].pitch * 5.2, STAGE_CENTER.z)
			this._renderScene()
			if (!silent) uni.showToast({ title: `已切换到${seat.row}排${seat.col}座视角`, icon: 'none' })
		},
		backToOverview() {
			this.currentSeat = null
			this.orbit.theta = 1.57
			this.orbit.phi = 0.98
			this.orbit.dist = 26
			this._renderScene()
			uni.showToast({ title: '已返回俯瞰视角', icon: 'none' })
		},
		confirmSeat() {
			if (!this.currentSeat) return
			const seatId = this.currentSeat.seatId
			if (this.selectedSeats.indexOf(seatId) >= 0) return
			this.selectedSeats = [...this.selectedSeats, seatId]
			this.currentSeat = null
			this._updateSeatColors()
			this._renderScene()
			uni.showToast({ title: '已加入参考座位', icon: 'none' })
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
			const seats = this.selectedSeats.join('、')
			uni.showModal({
				title: '参考完成',
				content: `已加入参考座位：${seats}\n\n本页面仅用于展示选座与观演视角，不提供在线支付。`,
				showCancel: false,
				confirmText: '知道了'
			})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 178rpx; min-height: 100vh; background: radial-gradient(circle at top, rgba(255,188,90,0.12), transparent 26%), linear-gradient(180deg, #090b15 0%, #131728 38%, #0d1020 100%); }
.header-card { display: flex; justify-content: space-between; gap: 24rpx; padding: 28rpx; margin-bottom: 24rpx; border-radius: 28rpx; background: linear-gradient(135deg, rgba(138,15,33,0.9), rgba(19,23,40,0.96)); box-shadow: 0 24rpx 50rpx rgba(0,0,0,0.28); border: 1rpx solid rgba(255,220,170,0.18); }
.eyebrow { font-size: 20rpx; letter-spacing: 4rpx; color: rgba(255,225,188,0.72); display: block; margin-bottom: 12rpx; }
.perf-name { font-size: 38rpx; font-weight: bold; color: #fff6e9; display: block; margin-bottom: 12rpx; }
.perf-meta { font-size: 24rpx; color: rgba(255,241,220,0.82); display: block; margin-bottom: 8rpx; }
.perf-note { font-size: 22rpx; color: rgba(255,214,153,0.88); display: block; line-height: 1.5; }
.badge-card { width: 128rpx; height: 128rpx; border-radius: 24rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(180deg, rgba(255,232,194,0.18), rgba(255,232,194,0.06)); }
.badge-value { font-size: 44rpx; color: #ffd391; font-weight: bold; }
.badge-label { font-size: 20rpx; color: rgba(255,226,188,0.7); }
.control-card { padding: 24rpx; border-radius: 24rpx; background: rgba(12,15,27,0.84); border: 1rpx solid rgba(255,231,190,0.1); box-shadow: 0 18rpx 42rpx rgba(0,0,0,0.16); margin-bottom: 20rpx; }
.control-title { font-size: 30rpx; color: #fff2db; font-weight: bold; display: block; margin-bottom: 8rpx; }
.control-subtitle, .mode-desc { font-size: 22rpx; color: rgba(255,238,211,0.68); line-height: 1.5; display: block; }
.mode-row { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 20rpx; }
.mode-chip { padding: 14rpx 24rpx; border-radius: 999rpx; background: rgba(255,255,255,0.04); border: 1rpx solid rgba(255,255,255,0.08); font-size: 22rpx; color: rgba(255,239,214,0.72); }
.mode-chip.active { background: linear-gradient(135deg, #ffcf84, #c68b33); color: #381a00; font-weight: bold; border-color: transparent; }
.canvas-wrap { position: relative; width: 100%; height: 860rpx; margin-bottom: 18rpx; border-radius: 30rpx; overflow: hidden; background: linear-gradient(180deg, #04060d, #0a1120); border: 1rpx solid rgba(255,234,203,0.08); box-shadow: inset 0 0 120rpx rgba(255,184,73,0.05), 0 28rpx 54rpx rgba(0,0,0,0.22); }
.fallback-wrap { height: 100%; padding: 90rpx 24rpx 24rpx; box-sizing: border-box; display: flex; flex-direction: column; }
.fallback-stage { width: 72%; align-self: center; text-align: center; padding: 16rpx 0; border-radius: 999rpx; background: linear-gradient(135deg, #7c1a2b, #cb8a37); color: #fff7ed; font-size: 24rpx; font-weight: bold; margin-bottom: 26rpx; }
.fallback-seat-map { flex: 1; display: flex; flex-direction: column; gap: 20rpx; justify-content: center; padding: 10rpx 0 24rpx; }
.fallback-row { display: flex; align-items: center; gap: 12rpx; transform: perspective(900rpx) rotateX(16deg); transform-origin: center top; }
.fallback-row-label { width: 28rpx; color: rgba(255,238,211,0.72); font-size: 22rpx; text-align: center; }
.fallback-row-seats { flex: 1; display: flex; flex-wrap: nowrap; gap: 10rpx; justify-content: center; }
.fallback-seat { width: 48rpx; height: 48rpx; border-radius: 14rpx 14rpx 10rpx 10rpx; display: flex; align-items: center; justify-content: center; font-size: 20rpx; color: #fff7ed; box-shadow: inset 0 -6rpx 0 rgba(0,0,0,0.18); }
.fallback-seat.available { background: #7b1630; border: 1rpx solid rgba(255,214,161,0.18); }
.fallback-seat.selected { background: #d7a34d; color: #3c2300; }
.fallback-seat.sold { background: #6d7580; color: rgba(255,255,255,0.7); }
.fallback-tip { margin-top: 18rpx; font-size: 22rpx; color: rgba(255,238,211,0.68); line-height: 1.6; }
.stage-tag { position: absolute; top: 20rpx; left: 24rpx; z-index: 2; padding: 10rpx 18rpx; border-radius: 999rpx; background: rgba(104,17,28,0.78); color: #ffd9aa; font-size: 20rpx; letter-spacing: 3rpx; }
.three-canvas { width: 100%; height: 100%; display: block; }
.canvas-hint { font-size: 23rpx; color: rgba(255,238,211,0.7); margin: 6rpx 6rpx 18rpx; line-height: 1.6; }
.seat-info { background: linear-gradient(180deg, rgba(255,247,232,0.98), rgba(248,233,206,0.95)); border-radius: 24rpx; padding: 26rpx; margin-bottom: 24rpx; }
.seat-text { font-size: 34rpx; color: #40170b; font-weight: bold; display: block; margin-bottom: 10rpx; }
.seat-summary { font-size: 24rpx; color: #6b4b2f; line-height: 1.6; display: block; }
.seat-metrics { display: flex; gap: 16rpx; margin: 18rpx 0 22rpx; }
.metric-card { flex: 1; padding: 18rpx; border-radius: 18rpx; background: rgba(255,255,255,0.56); }
.metric-label { display: block; font-size: 20rpx; color: #9c6e3f; margin-bottom: 8rpx; }
.metric-value { display: block; font-size: 28rpx; color: #51210d; font-weight: bold; }
.seat-info-btns { display: flex; gap: 16rpx; align-items: center; }
.btn-back-view, .btn-seat-confirm { flex: 1; font-size: 26rpx; padding: 0; line-height: 88rpx; border-radius: 18rpx; border: none; }
.btn-back-view { background: rgba(67,45,28,0.1); color: #5d3c23; }
.btn-seat-confirm { background: linear-gradient(135deg, #8e1528, #d39a43); color: #fff7ed; }
.view-tabs { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.tab { flex: 1; text-align: center; padding: 20rpx; background: rgba(255,246,227,0.94); border-radius: 16rpx; font-size: 26rpx; color: #5b4129; }
.seat-view { background: linear-gradient(180deg, rgba(8,10,19,0.95), rgba(18,20,36,0.95)); border-radius: 24rpx; padding: 32rpx; min-height: 360rpx; }
.stage-mini { background: linear-gradient(135deg, #7c1a2b, #cb8a37); color: #fff7ed; text-align: center; padding: 14rpx; border-radius: 999rpx; margin-bottom: 24rpx; font-size: 24rpx; font-weight: bold; }
.seats-in-section { display: flex; flex-wrap: wrap; gap: 16rpx; justify-content: center; }
.seat-3d { width: 76rpx; height: 76rpx; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; font-size: 22rpx; color: #fff7ed; }
.seat-3d.available { background: #7b1630; }
.seat-3d.selected { background: #d7a34d; color: #3c2300; }
.legend-card { padding: 18rpx 22rpx; border-radius: 20rpx; background: rgba(255,247,234,0.93); box-shadow: 0 12rpx 26rpx rgba(0,0,0,0.08); }
.legend { display: flex; flex-wrap: wrap; gap: 26rpx; }
.legend-item { display: flex; align-items: center; gap: 12rpx; font-size: 24rpx; color: #62462c; }
.dot { width: 28rpx; height: 28rpx; border-radius: 8rpx; }
.dot.available { background: #8f152a; }
.dot.selected { background: #d7a34d; }
.dot.sold { background: #6d7580; }
.bottom-bar { position: fixed; left: 20rpx; right: 20rpx; bottom: 20rpx; display: flex; align-items: center; padding: 20rpx 22rpx; background: rgba(15,18,30,0.92); border-radius: 26rpx; box-shadow: 0 18rpx 42rpx rgba(0,0,0,0.28); gap: 20rpx; }
.selected-sum { flex: 1; font-size: 23rpx; color: rgba(255,237,210,0.78); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.btn-confirm { background: linear-gradient(135deg, #8f152a, #d6a24a); color: #fff8ef; padding: 22rpx 36rpx; border-radius: 999rpx; font-size: 28rpx; font-weight: bold; }
.btn-confirm.disabled { background: rgba(255,255,255,0.18); color: rgba(255,255,255,0.45); }
</style>
