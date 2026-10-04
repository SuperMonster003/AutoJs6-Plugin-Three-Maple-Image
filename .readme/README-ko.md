<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>이미지 보기, 편집 및 형식 변환</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어 (Languages)

******

현재 README.md는 다음 언어를 지원합니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### 시작하기

독립 홈 화면에서 로컬 이미지를 보고 편집하거나 변환하고 원하는 위치에 결과 저장. 공통 설정 화면에서 언어, 야간 모드, 테마 색상 및 네 가지 런처 아이콘 선택 가능.

앱 ID가 `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools`에서 `io.github.supermonster003.autojs6.plugin.three.maple.image`(으)로 변경됩니다. Android는 별도 앱으로 설치하며 기존 앱과 데이터를 유지할 수 있고 설정은 자동으로 이전되지 않습니다.

******

### 소개

******

Image Viewer와 Image Tools를 3-Maple Image로 통합하여 이미지 보기, 편집 및 형식 변환 제공.

보기 작업은 입력을 읽기 전용으로 사용합니다. 편집과 변환은 별도 출력 파일을 생성하고 원본을 유지합니다. 독립 앱은 Android 파일 선택기를 사용합니다.

******

### 주요 기능

******

- 선택한 이미지만 열기: AutoJs6 파일 관리자의 다중 선택 모드에서 같은 폴더의 지원 이미지 최대 128개를 선택하고 `이미지 보기`를 탭합니다. 뷰어는 호스트의 선택 순서를 유지하며 이 그룹 안에서만 넘깁니다.
- 탭하고 탐색: 지원 이미지를 탭하면 뷰어가 바로 열립니다. 1배에서 좌우로 밀면 같은 폴더의 지원 이미지를 자연스러운 파일 이름 순서로 볼 수 있습니다.
- 자연스러운 제스처: 손가락을 중심으로 한 핀치 확대/축소 (최대 5배), 한 손가락 이동, 두 번 탭하여 화면 맞춤과 탭한 지점 중심의 2.5배 확대 전환, 시계 방향 90° 보기 회전, 화면 탭으로 컨트롤 숨기기/표시. 핀치 중과 두 번 탭한 뒤에는 현재 배율이 잠시 표시됩니다.
- 초대형 이미지도 선명하게 확대: JPEG, PNG 및 정적 HEIC/HEIF가 기기 텍스처 한도나 제한된 디코딩 예산을 넘으면 저해상도 미리보기를 먼저 표시하고 확대할 때 현재 보이는 영역의 고해상도 타일만 디코딩합니다. 타일 메모리는 상한이 있으며 페이지 전환이나 메모리 압박 시 즉시 해제됩니다.
- 핵심 정보를 한눈에: 오버레이 제목 바에는 파일 이름과, 여러 이미지를 열었을 때 `3 / 12` 형식의 페이지 번호가, 하단 정보 바에는 MIME 유형, 파일 크기, 디코딩된 해상도 (가로 x 세로)가 표시됩니다. Android 8.0 이상에서 디코더가 제공하는 경우 디코딩 픽셀 심도 (bpp)와 출력 색 공간도 표시됩니다.
- 상세 정보 하단 시트: `상세 정보`를 탭하면 드래그할 수 있는 시트가 열리며 파일 이름, MIME 유형, 크기, 해상도와 함께 EXIF가 있는 경우 촬영 시각, 기기, 노출 및 방향을 확인할 수 있습니다. GPS 메타데이터가 있으면 존재 여부만 알리고 좌표는 숨깁니다. 사진은 수동 보기 회전을 적용하기 전에 EXIF 방향에 따라 자동으로 회전하거나 반전됩니다.
- 인쇄 또는 PDF 저장: 오른쪽 위 메뉴의 `인쇄 / PDF로 저장`은 EXIF 보정과 수동 보기 회전을 유지한 현재 이미지 전체를 Android 시스템 인쇄 화면으로 보냅니다. 애니메이션 GIF는 탭할 때 보이는 프레임을 사용합니다; 확대와 이동은 출력을 자르지 않으며 플러그인은 임시 이미지나 PDF 파일을 만들지 않습니다.
- 일반적인 형식 기본 지원: JPEG 계열 (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF, AVIF까지 총 11가지 확장자를 지원하며 GIF 애니메이션은 자동 반복 재생되고 전용 일시중지/계속 컨트롤을 제공합니다.
- 공유와 전달: 한 번의 탭으로 시스템 공유 시트를 열 수 있고, 편집이나 주석이 필요하면 `다른 앱`을 사용하세요. 앱 목록에서 이 플러그인 자신은 자동으로 제외됩니다.
- 시스템 이미지 뷰어로도 사용 가능: 독립된 Android `ACTION_VIEW` 진입점이 다른 앱의 읽기 전용 이미지 보기 요청을 안전하게 처리합니다.
- 편집기는 자르기 비율 사전 설정, 90도 회전과 -45°~+45° 미세 회전, 좌우/상하 뒤집기, 밝기, 대비, 채도, 색온도, 브러시와 스타일 텍스트를 제공하며 조정 중 실시간 미리 보기를 지원합니다.
- 브러시는 펜, 형광펜, 개인정보 보호용 모자이크, 지우개를 지원하고 색상과 굵기를 기억합니다. 텍스트는 여러 줄, 크기, 색상, 윤곽선, 그림자와 드래그 배치를 지원합니다.
- 실행 취소와 다시 실행은 192 MiB 예산 안에서 최대 8개의 기록 스냅샷을 보관합니다. `원본 복원` 자체도 취소할 수 있으며 저장하지 않은 변경이 있으면 나갈 때 확인이 필요합니다.
- 편집기 저장 대화상자에서 원본 형식을 따르거나 JPEG, PNG, WebP를 선택하고 손실 품질을 조정하며 Android 11+에서 무손실 WebP를 켤 수 있습니다. 호스트는 원본을 덮어쓰지 않고 같은 위치에 새 파일을 게시합니다.
- 변환기는 JPEG / PNG / WebP, 품질 1-100 (기본 92), JPEG와 손실 WebP의 목표 파일 크기, Android 11+ 무손실 WebP, 256색 이하 이미지의 자동 인덱스 PNG 최적화를 지원합니다.
- 크기 모드는 `원본`, `백분율` (1-1000), 비율 고정이 가능한 `사용자 지정`, 기본 1920 px이며 확대하지 않는 `긴 변` 네 가지입니다. JPEG는 투명 영역을 흰색이나 검정으로 채울 수 있습니다.
- 대화상자는 해상도와 예상 크기를 실시간으로 표시합니다. 안전한 EXIF 유지는 기본적으로 꺼져 있으며, 켜도 제한된 카메라 필드만 유지하고 방향을 정규화하며 GPS와 포함된 미리 보기를 항상 제거합니다.
- 구성 변경에도 캔버스, 대화상자 초안, 실행 취소/다시 실행 기록과 진행 중인 작업을 보존합니다. 각 작업은 읽기 전용 입력 하나와 호스트가 소유한 일회성 같은 위치 출력 트랜잭션 하나만 사용합니다.

******

### 스크린샷

******

다음 화면은 Android 13 에뮬레이터에서 실행한 AutoJs6 6.8.0의 실제 UI 캡처입니다. 표시된 이미지, 파일 이름, 디렉터리는 모두 문서화를 위해 생성한 합성 데이터이며 개인 정보를 포함하지 않습니다.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="파일 관리자의 단일 이미지 보기 동작" width="360" />
      <br />
      <sub>파일 관리자의 단일 이미지 보기 동작</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="선택한 이미지 2개로 구성된 정확한 그룹" width="360" />
      <br />
      <sub>선택한 이미지 2개로 구성된 정확한 그룹</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="뷰어 기본 화면과 실시간 메타데이터" width="360" />
      <br />
      <sub>뷰어 기본 화면과 실시간 메타데이터</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="몰입형 2.5배 확대" width="360" />
      <br />
      <sub>몰입형 2.5배 확대</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="시스템 공유 패널" width="360" />
      <br />
      <sub>시스템 공유 패널</sub>
    </td>
  </tr>
</table>

******

### 설치 및 사용

******

시작하기 전에 다음 요구 사항을 확인하세요:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

설치부터 첫 이미지를 보는 것까지 4단계입니다:

1. 독립 홈 화면에서 로컬 이미지를 보고 편집하거나 변환하고 원하는 위치에 결과 저장.
2. AutoJs6를 열고 `플러그인 센터`에 들어가 `이미지 뷰어`를 찾아 활성화합니다.
3. AutoJs6 파일 관리자에서 지원되는 아무 이미지 파일 (예: `screenshot.png`)을 찾습니다.
4. 파일을 탭하면 이미지가 전용 뷰어에서 열립니다.

뷰어 안에서: 1배에서 좌우로 밀면 같은 폴더의 이전 또는 다음 지원 이미지로 이동합니다. 핀치 동작으로 손가락 위치를 중심으로 확대/축소하고 (1배에서 5배), 한 손가락으로 끌어 이동하며, 두 번 탭하여 화면 맞춤과 탭한 지점 중심의 2.5배 확대를 전환합니다. 핀치 중과 두 번 탭한 뒤에는 현재 배율이 잠시 표시됩니다. `회전`을 탭하면 현재 보기만 회전합니다. 활성 확대는 유지되며, 오른쪽 위 메뉴의 `확대 재설정`은 원래 방향과 화면 맞춤을 함께 복원합니다. GIF 애니메이션에는 플로팅 `애니메이션 일시중지` / `애니메이션 계속` 버튼이 표시되며 정적 이미지에는 표시되지 않습니다. 이미지를 탭하면 오버레이 막대 표시를 전환하고, `상세 정보`를 탭하면 파일 정보와 EXIF 항목이 담긴 하단 시트가 열립니다. `공유`와 `다른 앱`은 처음 연 이미지에서 사용할 수 있습니다. 세션으로만 연 이웃 페이지에는 전달 가능한 content URI가 의도적으로 없으므로 두 동작이 비활성화됩니다.

******

### 지원 형식

******

파일 관리자의 보기 동작은 다음 확장자와 정확히 일치합니다:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

JFIF와 JPE는 JPEG 계열의 별칭 확장자입니다. HEIC와 HEIF에는 Android 9 이상, AVIF에는 Android 12 이상이 필요합니다. 뷰어는 작은 로컬 디코더 기능 검사도 실행하며 플랫폼 디코더를 사용할 수 없을 때 구체적인 메시지를 표시합니다. 독립된 `ACTION_VIEW` 진입점은 `image/*` MIME 유형으로 요청을 받으므로 위 목록에 제한되지 않습니다. 파일 하나의 최대 크기는 8 TiB이며, 실제 디코딩 지원은 Android 플랫폼과 Glide에 따라 달라집니다.

******

### 자주 묻는 질문

******

**파일 메뉴에 `이미지 편집`과 `이미지 변환`이 보이지 않나요?**

다음 순서로 확인하세요: AutoJs6 버전 코드가 5276 이상인지; 플러그인이 `플러그인 센터`에서 활성화되어 있는지; 파일 확장자나 MIME 유형이 지원 목록에 있는지. 셋 중 하나라도 충족하지 않으면 메뉴 동작이 나타나지 않습니다.

**열 때 `이미지 정보를 읽을 수 없습니다`라고 표시되거나 화면이 바로 닫히나요?**

보기 작업은 입력을 읽기 전용으로 사용합니다. 편집과 변환은 별도 출력 파일을 생성하고 원본을 유지합니다. 독립 앱은 Android 파일 선택기를 사용합니다.

**처리 결과는 어디에 저장되나요? 원본을 덮어쓰나요?**

이미지 보기는 선택한 여러 이미지를 지원하고 편집과 변환은 한 번에 한 장을 처리합니다. 독립 홈 화면에서는 저장 위치를 선택할 수 있으며 AutoJs6 호출은 원본 옆에 새 파일을 생성합니다.

**큰 이미지를 변환할 때 메모리 부족이나 픽셀 수 초과 경고가 나오나요?**

출력 크기에는 세 가지 제한이 있습니다: 한 변이 16384 px를 넘을 수 없고, 총 픽셀 수가 4천만 (40 MP)을 넘을 수 없으며, 기기 메모리 예산 안에 들어야 합니다. 원본이 제한을 넘는 경우 `크기 조정`을 `백분율`, `긴 변` 또는 `사용자 지정`으로 바꿔 출력을 줄이세요. 메모리 부족은 다른 앱을 종료하거나 해상도를 더 낮추면 대개 해결됩니다.

**편집 후 저장한 이미지의 해상도가 왜 낮아졌나요?**

편집을 부드럽고 안정적으로 유지하기 위해, 편집 픽셀 예산 (기기 메모리에 따라 최대 약 16 MP)을 넘는 이미지는 다운샘플링을 거쳐 편집기에 올라가며, 저장 결과는 편집 캔버스의 해상도와 같습니다. 픽셀을 건드리지 않고 형식이나 크기만 바꾸려면 `이미지 변환`을 사용하세요. 출력 크기에 맞춰 정밀하게 디코딩하므로 이 예산의 제한을 받지 않습니다.

**여러 이미지를 한 번에 처리하거나 결과를 다른 디렉터리에 저장할 수 있나요?**

이미지 보기는 선택한 여러 이미지를 지원하고 편집과 변환은 한 번에 한 장을 처리합니다. 독립 홈 화면에서는 저장 위치를 선택할 수 있으며 AutoJs6 호출은 원본 옆에 새 파일을 생성합니다.

******

### 보안

******

플러그인은 기본 거부 원칙으로 만들어졌습니다. 다음 조치는 모두 항상 켜져 있으며 끌 수 없습니다:

- 보기 작업은 입력을 읽기 전용으로 사용합니다. 편집과 변환은 별도 출력 파일을 생성하고 원본을 유지합니다. 독립 앱은 Android 파일 선택기를 사용합니다.

******

### 플러그인 인터페이스 (개발자용)

******

호스트는 다음 식별 정보로 플러그인을 검색하고 호출합니다:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: three-maple-image
engine: explorer-action
variant: default
explorer action id: view-image
protocol version: 12
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5276
```

현재 구현은 explorer-action 프로토콜 버전 12을 기반으로 합니다: 기본 동작은 단일 파일 대상, 읽기 전용 접근, `readSiblings`를 선언합니다. 호스트 세션은 바로 이웃한 파일만 노출합니다. 플러그인은 지원되고 읽을 수 있으며 심볼릭 링크가 아닌 이미지만 남기고, 자연스러운 파일 이름 순서를 적용하며, 선택 이미지 주변에 최대 128페이지의 제한된 창을 유지합니다. 두 번째 읽기 전용 선택 도구 모음 동작은 `readSiblings` 없이 여러 파일을 선언하고, 같은 상위 폴더의 지원 이미지 1개에서 128개까지 받아 호스트 선택 순서를 유지하며 명시적으로 허용된 대상만 전달합니다. 편집, 변환, 파일 정보, 삭제, 이동, 이름 변경은 계속 호스트 기능입니다. 플러그인이 없으면 호스트는 읽기 전용 외부 `ACTION_VIEW` 요청으로 대체합니다.

******

### 로드맵

******

완료된 기능과 향후 계획은 체크 가능한 목록으로 ROADMAP.md에서 관리합니다. 체크되지 않은 항목은 의향을 나타낼 뿐 현재 기능을 설명하지 않습니다.

- [체크 가능한 ROADMAP.md 열기](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### 릴리스 이력

******

#### v2.0.0

###### 2026/10/04

* `힌트` 앱 ID가 io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools에서 io.github.supermonster003.autojs6.plugin.three.maple.image(으)로 변경됩니다. Android는 별도 앱으로 설치하며 기존 앱과 데이터를 유지할 수 있고 설정은 자동으로 이전되지 않습니다
* `기능` Image Viewer와 Image Tools를 3-Maple Image로 통합하여 이미지 보기, 편집 및 형식 변환 제공
* `기능` 독립 홈 화면에서 로컬 이미지를 보고 편집하거나 변환하고 원하는 위치에 결과 저장
* `기능` 공통 설정 화면에서 언어, 야간 모드, 테마 색상 및 네 가지 런처 아이콘 선택 가능

#### v1.3.1

###### 2026/09/19

* `수정` 공유 빌드 플러그인 1.8.3을 통해 AGP 9.1의 SDK XML v4 파싱 경고 및 JVM 단위 테스트 조립 작업에서 APK 네이티브 라이브러리 정렬 검사가 잘못 실행되는 문제 해결
* `개선` compileSdk 와 targetSdk 를 37 (Android 17) 로 올리며, 플러그인 동작은 새 대상 버전의 영향을 받지 않음

#### v1.3.0

###### 2026/09/13

* `기능` 화면에서 현지화된 로컬 릴리스 기록을 표시하고 영어 대체 제공
* `개선` 릴리스 서명 설정, 예상 APK 구성 및 문서 재생성 결과 검증

##### 전체 이력

* [CHANGELOG-ko.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-ko.md)

******

### 빌드

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release 빌드:

```powershell
.\gradlew.bat :app:assembleRelease
```

빌드 매개 변수는 `version.properties`에서 가져옵니다. 현재 최소 SDK는 24, 대상 SDK는 36입니다.

******

### 리소스 구조

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml`은 플러그인 정보와 뷰어 UI를 현지화하고, `plugin_instruction.md`는 호스트가 표시하는 사용 설명을 제공합니다. 모든 README와 CHANGELOG는 `.python/generate_markdown.py`가 JSON 소스에서 생성합니다: 문서를 수정할 때는 `.readme`와 `.changelog` 아래의 `lang_*.json`을 편집한 뒤 스크립트를 다시 실행하고, 생성된 Markdown 파일을 직접 편집하지 마세요.

******

### 링크

******

- AutoJs6 문서: https://docs.autojs6.com
- Android 안전한 파일 공유: https://developer.android.com/training/secure-file-sharing
- Glide (이미지 로딩 및 렌더링 엔진): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### 출처 및 감사의 말

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
