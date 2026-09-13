<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>이미지 보기 및 상세 정보 확인</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어 (Languages)

******

현재 README.md는 다음 언어를 지원합니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### 소개

******

Image Viewer는 AutoJs6 파일 관리자를 위한 이미지 탐색 플러그인입니다. JPG, PNG, GIF, WEBP 같은 파일을 탭하면 전용 뷰어가 열립니다: 이미지는 화면에 자동으로 맞춰지고, 핀치 동작으로 세부를 살펴볼 수 있으며, 1배에서는 좌우로 밀어 같은 폴더의 지원 이미지를 연속으로 볼 수 있습니다. 페이지마다 제목과 메타데이터가 갱신되고, 처음 연 이미지는 공유하거나 다른 앱에 넘길 수 있습니다.

이 플러그인은 한 가지 일을 안전하게 수행합니다: 읽기 전용 보기입니다. 처음 탭한 파일은 임시 읽기 전용 content URI로 전달되고, 바로 이웃한 파일은 호스트가 소유한 단기 readSiblings 세션을 통해서만 열거하고 엽니다. 저장소나 네트워크 권한을 요청하지 않고, 원본 파일을 수정하거나 이동하지 않으며, 뷰어와 함께 호스트 세션을 닫습니다.

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
- 읽기 전용 보안 샌드박스: 저장소와 네트워크 권한 없이 임시 읽기 전용 권한으로 파일 하나에만 접근하며, 원본 파일에는 절대 쓰지 않습니다.

******

### 스크린샷

******

다음 화면은 Android 13 에뮬레이터에서 실행한 AutoJs6 6.8.0의 실제 UI 캡처입니다. 표시된 이미지, 파일 이름, 디렉터리는 모두 문서화를 위해 생성한 합성 데이터이며 개인 정보를 포함하지 않습니다.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="파일 관리자의 단일 이미지 보기 동작" width="360" />
      <br />
      <sub>파일 관리자의 단일 이미지 보기 동작</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="선택한 이미지 2개로 구성된 정확한 그룹" width="360" />
      <br />
      <sub>선택한 이미지 2개로 구성된 정확한 그룹</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="뷰어 기본 화면과 실시간 메타데이터" width="360" />
      <br />
      <sub>뷰어 기본 화면과 실시간 메타데이터</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="몰입형 2.5배 확대" width="360" />
      <br />
      <sub>몰입형 2.5배 확대</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="시스템 공유 패널" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

설치부터 첫 이미지를 보는 것까지 4단계입니다:

1. 플러그인 APK를 다운로드하여 설치합니다. 플러그인에는 런처 아이콘이 없으며 설치 후 AutoJs6가 전적으로 관리합니다.
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

**이미지 파일을 탭해도 이 뷰어가 열리지 않나요?**

다음 순서로 확인하세요: AutoJs6 버전 코드가 5276 이상인지 (버전 6.8.0 이상이면 충족); 플러그인이 `플러그인 센터`에서 활성화되어 있는지; 파일 확장자가 지원 목록에 있는지. 셋 중 하나라도 충족되지 않으면 탭은 이 플러그인으로 처리되지 않습니다.

**열었더니 `이미지를 표시할 수 없습니다`라고 나오나요?**

흔한 원인: 이미지 데이터가 손상되었거나 인코딩이 현재 Android 플랫폼에서 지원되지 않는 경우; 여는 순간 파일이 이동, 이름 변경, 삭제된 경우; 선언된 파일 크기가 실제 크기와 일치하지 않는 경우 (보안 검증이 이런 요청을 거부합니다).

**이미지를 편집, 자르기 또는 영구적으로 회전할 수 있나요?**

아니요. 이 플러그인은 읽기 전용 보기에 집중합니다. `회전`은 현재 보기만 바꾸며 원본 파일은 절대 변경하지 않습니다. 편집이 필요하면 `다른 앱`을 탭해 편집 앱에 넘기세요. 삭제, 이동, 이름 변경 같은 파일 관리 작업은 계속 AutoJs6 파일 관리자가 제공합니다.

**GIF 애니메이션이 재생되나요?**

재생됩니다. 애니메이션은 Glide가 디코딩하여 자동으로 반복 재생합니다. 뷰어는 실제로 재생 가능한 애니메이션에만 일시중지/계속 컨트롤을 표시하며, 이 동작은 화면 재생에만 적용되고 원본 파일을 수정하지 않습니다.

**이 플러그인이 설치되어 있지 않으면 어떻게 되나요?**

호스트는 읽기 전용 외부 보기 요청으로 대체하며, 기기에 이미 있는 이미지 앱이 처리합니다. 이 플러그인을 설치하고 활성화하면 탭은 내장 뷰어에서 우선 열립니다.

**왜 시스템 수준의 이미지 보기 진입점도 등록하나요?**

이는 독립된 `ACTION_VIEW` 진입점으로, 읽기 전용 `content` URI의 `image/*` 요청만 받아 다른 앱이 이 뷰어를 사용할 수 있게 합니다. 파일 관리자 진입점과 격리되어 있고, 같은 엄격한 검증을 거치며, 마찬가지로 아무것도 쓰지 않습니다.

******

### 보안

******

플러그인은 기본 거부 원칙으로 만들어졌습니다. 다음 조치는 모두 항상 켜져 있으며 끌 수 없습니다:

- 제한된 명시적 선택 그룹: 다중 선택은 같은 상위 폴더의 지원되는 직접 파일 1개에서 128개까지만 허용합니다. 대상 ID, URI, 파일 이름, 순서가 있는 ClipData, MIME 유형, 크기는 필요한 항목에서 고유하고 서로 일치해야 하며, 뷰어를 열기 전에 선택한 각 이미지의 실제 내용을 다시 확인합니다.
- 민감 권한 제로: 저장소, 네트워크, 기타 런타임 권한을 요청하지 않고 평문 트래픽도 차단합니다. 파일 관리자 진입점과 웨이크 진입점은 호스트 플러그인 권한으로 보호되어 호스트만 호출할 수 있습니다.
- 임시로 범위가 제한된 읽기 전용 접근: 선택 파일은 임시 content URI를 사용하고, 바로 이웃한 파일은 v12 HOST_SESSION, 불투명 대상 ID, 검증된 직접 상대 이름으로만 열거하고 엽니다. 파일 시스템 경로를 받지 않으며, 쓰기 권한과 영구 권한은 거부합니다.
- 진입점 항목별 검증: 동작 식별자, 프로토콜 버전, 요청 UUID, 호스트 빌드, 호출 출처, 대상 Bundle, URI 구조, ClipData, 파일 이름, MIME 유형, 선언 크기, 직접 상위 관계, 세션 Binder 설명자를 하나씩 확인하며, 하나라도 어긋나면 열지 않습니다.
- 콘텐츠 이중 확인: 열기 전에 이미지 디코딩 경계를 조사하고 선언 크기와 실제 크기를 대조하여 불일치 시 거부합니다. 파일 하나의 상한은 8 TiB입니다.
- 격리된 이중 진입점: 파일 관리자 진입점과 외부 `ACTION_VIEW` 진입점은 서로 독립적이며, 후자는 읽기 전용 `content` URI 이미지 요청만 받아 같은 콘텐츠 검증을 통과해야 합니다.
- 뷰어는 외부에 공개되지 않음: 표시 화면은 플러그인 내부에서만 시작할 수 있고, 공유와 외부 열기도 임시 읽기 전용 권한만 전달하며, 원본 파일에는 절대 쓰지 않습니다.

******

### 플러그인 인터페이스 (개발자용)

******

호스트는 다음 식별 정보로 플러그인을 검색하고 호출합니다:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
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

- [체크 가능한 ROADMAP.md 열기](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### 릴리스 이력

******

#### v1.3.0

###### 2026/09/13

* `기능` 화면에서 현지화된 로컬 릴리스 기록을 표시하고 영어 대체 제공
* `개선` 릴리스 서명 설정, 예상 APK 구성 및 문서 재생성 결과 검증

#### v1.2.0

###### 2026/09/12

* `기능` 뷰어를 몰입형 전체 화면 레이아웃으로 재설계: 이미지가 상태 표시줄과 탐색 메뉴 아래까지 창 전체를 채우고, 상단과 하단 막대는 한 번 탭으로 숨기거나 다시 표시하는 반투명 오버레이로 바뀌었습니다
* `기능` 오버레이 제목 표시줄에 파일 이름을 표시하고, 폴더나 선택 그룹을 탐색할 때 `3 / 12` 형식의 페이지 번호를 표시하며, 오른쪽 위 메뉴에 `확대 재설정`과 `인쇄 / PDF로 저장`을 모았습니다
* `기능` 하단 작업 표시줄을 `상세 정보`, `회전`, `공유`, `다른 앱` 아이콘 버튼으로 바꾸고, 애니메이션 GIF에서만 나타나는 플로팅 일시중지 / 계속 버튼을 추가했습니다
* `기능` 이미지 상세 정보는 드래그할 수 있는 하단 시트에서 열리며 파일 이름, MIME 유형, 크기, 해상도, 디코딩 색상 정보와 EXIF 항목을 표시합니다; 아래로 스와이프하거나 이미지를 탭하거나 뒤로 가기를 눌러 닫을 수 있습니다
* `개선` 에지 투 에지 시스템 표시줄과 디스플레이 컷아웃을 처리하여 Android 15 이상에서 화면이 시스템 표시줄에 가려지지 않습니다
* `개선` 모든 아이콘 컨트롤에 이전 텍스트 라벨과 동일한 접근성 설명을 제공합니다
* `개선` 빌드 시 의도하지 않은 네이티브 의존성을 거부하고 JSON 보고서 생성

#### v1.1.0

###### 2026/08/31

* `힌트` explorer-action v12 기반의 같은 폴더 탐색과 명시적 다중 선택에는 AutoJs6 호스트 build 5276 이상이 필요
* `기능` 같은 폴더의 지원 이미지를 자연 파일명 순서로 스와이프해 탐색하거나, 호스트 선택 순서를 유지한 채 최대 128장의 명시적 선택 그룹 열기
* `기능` 터치 지점 중심 핀치 확대, 두 번 탭 2.5배 확대, 90도 보기 회전, 일시적 배율 표시, GIF 애니메이션 일시 정지/계속을 포함한 향상된 뷰어 조작
* `기능` 8가지 방향을 모두 자동 보정하고 GPS 좌표를 항상 숨기는 필요 시 EXIF 세부 정보, 제공되는 경우 디코딩 픽셀 깊이와 색 공간 표시, 전체 이미지 인쇄 또는 PDF 저장
* `기능` Android 9 이상에서 HEIC / HEIF, Android 12 이상에서 AVIF 지원, 실제 디코더 기능 탐지와 미지원 시 명확한 안내
* `기능` 초대형 JPEG / PNG 및 정적 HEIC / HEIF를 제한된 미리 보기와 현재 보이는 영역의 고해상도 타일로 메모리 예산 안에서 표시
* `개선` 모든 진입점에서 explorer-action v12 요청, 대상, 콘텐츠, 크기, 직계 하위 항목을 엄격히 검증하면서 임시 읽기 전용 접근과 저장소/네트워크 권한 없는 설계를 유지
* `개선` 10개 언어의 인터페이스와 사용자 문서를 확장하고 실제 Android UI에서 캡처한 5장의 갤러리 추가
* `의존성` EXIF 메타데이터를 읽기 전용으로 분석하기 위해 AndroidX ExifInterface 1.4.2 추가

##### 전체 이력

* [CHANGELOG-ko.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-ko.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
