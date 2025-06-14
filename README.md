# 💪 PushBarX – 실시간 자세 교정 및 생체 데이터 기반 IoT 푸시업 보조 시스템

시연영상 바로가기
[![시연 영상 바로가기](https://img.shields.io/badge/YouTube-Demo-red?logo=youtube)](https://youtu.be/yhWr8rOICiI)

## 👥 팀명: [D조] PushBarX  

---
![0603(1)](https://github.com/user-attachments/assets/0d3c8028-5561-476d-acbf-de1c0c595576)



## 📘 구현 목적 (Implementation Purpose)

본 프로젝트의 주요 목적은 **IoT 기술을 활용한 스마트 푸시업 보조 장치**를 개발하는 것입니다.  
사용자가 혼자서도 올바른 자세와 폼을 유지하며 운동할 수 있도록 도와주며, **부상을 방지하고 운동 효과를 극대화**할 수 있습니다.  
푸시업 바에 여러 센서를 통합하고 데이터를 모바일 앱과 연결함으로써, **실시간 피드백과 운동 성과 분석을 코치 없이도 제공**합니다.

---

## ⚙️ 기술 개요 (Technology Overview)

본 시스템은 사물인터넷(IoT) 기반으로 푸시업 중 사용자의 생체역학적, 생리학적 데이터를 수집하고 분석합니다.  
센서 데이터는 BLE(Bluetooth Low Energy)를 통해 스마트폰으로 전송되며, 앱을 통해 실시간 피드백을 제공합니다.

### 📌 중점 기능:
- 정확한 자세 교정  
- 좌우 압력 대칭 분석  
- 반복 횟수 측정  
- 심박수 및 칼로리 소모 모니터링  

### 🛠️ 사용 기술:
- Embedded Sensors  
- BLE 모듈 포함 마이크로컨트롤러  
- Android/iOS 앱  
- (옵션) 클라우드/웹 서버 연동

---

## 🔩 시스템 구성 요소 (센서)

| 센서 | 기능 | 위치 |
|------|------|------|
| **Heart Rate Sensor** | 맥박 측정 및 칼로리 추정 | 손잡이 근처 |
| **Laser Distance Sensor** | 푸시업 깊이 측정 | 손잡이 아래 또는 측면 |
| **Camera Sensor** | 팔 정렬, 자세 불균형 감지 | 바 앞면 또는 측면 |
| **Pressure Sensors (L/R)** | 양손 압력 비교 및 대칭성 판단 | 손잡이 내부 좌우 |
| **Gyroscope Sensor** | 기울기 및 회전 감지 | 본체 중심부 또는 연결부 |

---

