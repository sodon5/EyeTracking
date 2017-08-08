# EyeTracking

- Google vision API를 이용한 Googly eyes sample 오픈 소스를 사용함.
- 기존 프로젝트는 눈의 좌표를 얻어오는 데에만 사용함
- 이후에 눈이 바라보고 있는 곳에 대한 정보를 예측하기 위해, left_top, left_bottom, right_top, right_bottom 의 네 곳을 쳐다볼 때의 좌표를 버튼을 통해 저장시킴
- 그 기준점을 가지고, 화면의 9분할 중 어디를 쳐다보는지를 예측함
(9분할은 눈좌표의 부정확성에 대한 대안이었음)

* 눈 좌표의 부정확도가 생각보다 큰 문제였음
->> STOP 