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

- 앨범 홈: 단독으로 실행하면 기기의 이미지를 앨범 그리드로 보여 주며 (표지, 이름, 개수), 승인한 폴더는 별도 그룹으로 표시됩니다. 메뉴의 `모든 폴더 내용 표시`는 평면 보기를 엽니다. 미디어 그리드는 3-6열 (메뉴 또는 핀치), 이름, 수정 날짜, 촬영 날짜, 크기 정렬을 앨범별로 기억하며, 날짜 말풍선이 있는 빠른 스크롤바와 당겨서 새로고침을 제공합니다.
- 페이지 뷰어와 썸네일 바: 그리드에서 연 이미지는 앨범 전체를 좌우로 넘겨 볼 수 있고, 썸네일 바는 현재 페이지를 강조하며 탭하면 이동합니다. 돌아가면 그리드가 마지막으로 본 이미지까지 스크롤됩니다. 두 손가락으로 이미지를 회전하면 손을 뗄 때 가장 가까운 90도로 맞춰집니다 (표시만 변경, `설정` > `뷰어` > `두 손가락 회전`에서 끌 수 있음).
- 필요할 때만 권한 요청: 독립 홈은 처음 실행할 때 한 번만 사진 접근을 요청하고 이후에는 스스로 요청하지 않습니다. 허용하지 않아도 `이미지 선택`, `폴더 선택`, `이미지 편집`, `이미지 변환`은 시스템 선택기로 계속 사용할 수 있습니다. Android 14 이상에서는 선택한 사진만 허용 (`선택한 사진 관리`)할 수 있습니다. `설정` > `저장공간 권한`에서 다시 허용하고, 승인한 폴더 (사진 접근이 없어도 앨범으로 표시)와 선택 사항인 `모든 파일 접근`을 관리합니다. AutoJs6나 다른 앱에서 이미지를 열 때는 권한을 요청하지 않습니다.
- 앨범으로 보기: AutoJs6 파일 관리자의 폴더 메뉴에서 `앨범으로 보기`를 선택하면 해당 폴더의 이미지를 그리드로 보고 하위 폴더로 들어갈 수 있습니다. 호스트 세션을 통한 읽기 전용 접근이며 권한을 요청하거나 절대 경로를 표시하지 않습니다.
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
- 변환 페이지는 하단에 출력 해상도, 예상 크기와 접미사를 실시간으로 표시합니다. 안전한 EXIF 유지는 기본적으로 꺼져 있으며, 켜도 제한된 카메라 필드만 유지하고 방향을 정규화하며 GPS와 포함된 미리 보기를 항상 제거합니다.
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

1. 3-Maple Image를 단독으로 실행하면 기기의 앨범을 둘러볼 수 있습니다 (첫 실행 때 사진 접근을 한 번 요청). 오른쪽 위 메뉴에서는 여전히 로컬 이미지를 골라 보고 편집하거나 변환하고 원하는 위치에 결과를 저장할 수 있습니다.
2. AutoJs6를 열고 `플러그인 센터`에 들어가 `3-Maple Image`를 찾아 활성화합니다.
3. AutoJs6 파일 관리자에서 지원되는 아무 이미지 파일 (예: `screenshot.png`)을 찾습니다.
4. 파일을 탭하면 이미지가 전용 뷰어에서 열립니다. 폴더 메뉴에서 `앨범으로 보기`를 선택하면 해당 폴더의 이미지를 그리드로 볼 수 있습니다.

뷰어 안에서: 1배에서 좌우로 밀면 같은 폴더의 이전 또는 다음 지원 이미지로 이동합니다. 핀치 동작으로 손가락 위치를 중심으로 확대/축소하고 (1배에서 5배), 한 손가락으로 끌어 이동하며, 두 번 탭하여 화면 맞춤과 탭한 지점 중심의 2.5배 확대를 전환합니다. 핀치 중과 두 번 탭한 뒤에는 현재 배율이 잠시 표시됩니다. `회전`을 탭하면 현재 보기만 회전합니다. 활성 확대는 유지되며, 오른쪽 위 메뉴의 `확대 재설정`은 원래 방향과 화면 맞춤을 함께 복원합니다. GIF 애니메이션에는 플로팅 `애니메이션 일시중지` / `애니메이션 계속` 버튼이 표시되며 정적 이미지에는 표시되지 않습니다. 이미지를 탭하면 오버레이 막대 표시를 전환하고, `상세 정보`를 탭하면 파일 정보와 EXIF 항목이 담긴 하단 시트가 열립니다. `공유`와 `다른 앱`은 처음 연 이미지에서 사용할 수 있습니다. 세션으로만 연 이웃 페이지에는 전달 가능한 content URI가 의도적으로 없으므로 두 동작이 비활성화됩니다. 독립 실행 홈이나 다른 앱에서 연 이미지는 `편집하다`도 제공하여 편집기를 열고 결과를 저장할 수 있습니다. 호스트 진입과 이웃 페이지에서는 `편집하다`을 제공하지 않습니다.

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

**단독으로 실행하면 왜 사진 접근을 요청하나요? 거부해도 사용할 수 있나요?**

앨범 홈은 기기의 이미지 목록이 필요하므로 단독으로 처음 실행할 때 한 번 요청합니다 (Android 13 이상은 사진 및 동영상, 이전 버전은 저장공간). 이후에는 스스로 요청하지 않습니다. 접근 권한이 없어도 `이미지 선택`, `폴더 선택`, `이미지 편집`, `이미지 변환`은 시스템 선택기로 계속 사용할 수 있으며, 나중에 홈 화면 버튼이나 `설정` > `저장공간 권한`에서 허용할 수 있습니다. 시스템이 더 이상 대화상자를 보여 주지 않으면 버튼이 `앱 설정 열기`로 바뀝니다. AutoJs6나 다른 앱에서 이미지를 열 때는 어떤 권한도 요청하지 않습니다.

**Android 14 이상에서 일부 사진만 허용했더니 다른 앨범이 보이지 않아요.**

제한된 접근은 시스템 사진 선택 도구에서 고른 사진만 공개하므로 해당 사진이 있는 앨범만 표시됩니다. 홈 화면 위쪽의 `선택한 사진 관리`를 탭해 선택을 바꾸거나 `설정` > `저장공간 권한`에서 전체 접근을 다시 허용하세요.

**`모든 파일 접근`은 무엇에 쓰나요? 켜야 하나요?**

아니요. 시스템 설정 페이지를 여는 선택 사항인 고급 스위치이며, 앨범을 둘러보는 데는 사진 접근만 필요합니다. 이후 버전의 숨김 폴더 (.nomedia)와 확인 없는 일괄 작업을 위해 마련되어 있으며, 시스템 설정에서 언제든 끌 수 있습니다.

**파일 메뉴에 `이미지 편집`과 `이미지 변환`이 보이지 않나요?**

다음 순서로 확인하세요: AutoJs6 버전 코드가 5276 이상인지; 플러그인이 `플러그인 센터`에서 활성화되어 있는지; 파일 확장자나 MIME 유형이 지원 목록에 있는지. 셋 중 하나라도 충족하지 않으면 메뉴 동작이 나타나지 않습니다.

**열 때 `이미지 정보를 읽을 수 없습니다`라고 표시되거나 화면이 바로 닫히나요?**

현재 파일을 읽을 수 없는 상태입니다. 파일이 이동되거나 삭제되었거나, 공유한 앱이 접근 권한을 거둬들였거나, 내용이 확장자와 맞지 않을 (예: 손상된 파일) 수 있습니다. 현재 위치에서 다시 열거나 원래 앱에서 다시 공유하세요.

**처리 결과는 어디에 저장되나요? 원본을 덮어쓰나요?**

원본은 덮어쓰지 않습니다. AutoJs6에서 호출하면 결과가 원본 옆에 새 파일로 저장되고, 독립 홈 화면에서 시작하면 편집이나 변환 후 저장 위치를 직접 고릅니다.

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
- AutoJs6 (`이미지 보기`, `앨범으로 보기`)나 다른 앱에서 이미지를 열 때는 런타임 권한을 요청하지 않습니다. 독립 홈이 스스로 사진 접근을 요청하는 것은 최대 한 번이며, `모든 파일 접근`은 사용자가 시스템 설정에서 켤 때만 활성화됩니다.
- 플러그인은 네트워크 권한 (`INTERNET`)을 선언하지 않습니다. 앨범, 썸네일, 이미지 세부 정보는 기기에서 읽히며 기기 밖으로 나가지 않습니다.

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

#### v2.1.0

###### 2026/10/13

* `힌트` APK에 Jetpack Compose가 포함하는 네이티브 라이브러리 libandroidx.graphics.path.so (arm64-v8a, armeabi-v7a, x86_64, x86 각 약 10 KB)가 추가되며, 플러그인 자체는 여전히 네이티브 코드를 포함하지 않고 단일 범용 APK로 유지
* `힌트` 설치 직후 처음 몇 번의 실행은 오래된 기기에서 약간 느리며 시스템의 백그라운드 최적화가 끝나면 정상으로 돌아옵니다
* `기능` 전체 인터페이스를 Material 3 (Jetpack Compose) 로 전환: 홈, 설정, 정보, 버전 기록, 뷰어, 편집기, 변환기를 다시 만들어 테마 색상과 다크 모드를 따르고 큰 글꼴과 오른쪽에서 왼쪽 레이아웃을 지원
* `기능` 편집기 도구 모음을 아이콘과 레이블이 있는 그룹 도구 스트립 (변형, 조정, 그리기, 텍스트) 으로 변경. 매개변수 패널은 현재 도구를 따르며 텍스트, 저장, 팔레트 대화상자를 다시 구성
* `기능` 변환기를 대화상자에서 전체 페이지 양식으로 변경. 현재 크기 모드의 입력만 표시하고 출력 해상도, 예상 크기, 출력 접미사를 하단에 고정 표시
* `기능` 뷰어 하단 막대를 상세 정보, 회전, 편집, 공유, 다른 앱 다섯 개 버튼으로 변경. 독립 실행 홈이나 다른 앱에서 연 이미지는 바로 편집기로 이동해 결과를 저장 가능
* `수정` 앱 내 버전 기록 페이지가 비어 있던 문제 (생성된 파일의 ## 버전 제목을 인식하지 못함)
* `수정` 앱 언어를 아랍어로 바꾼 뒤 이미 열린 페이지가 왼쪽에서 오른쪽 레이아웃을 유지하던 문제
* `수정` 라이트 모드에서 뷰어와 편집기의 상태 표시줄 아이콘이 페이지 테마를 따라 어두워져 어두운 캔버스 위에서 보이지 않던 문제
* `개선` 플러그인 센터 아이콘에 Icon Studio에서 조정한 크기, 위치, 밝은 이미지와 어두운 이미지 및 원형 배경을 적용하고 재생성 가능한 원본과 매개변수를 유지
* `개선` Android 앱 정보 아이콘에 Icon Studio의 그림과 밝은 배경 및 어두운 배경을 사용하고 플러그인 센터의 투명 그림과 기존 런처 옵션을 유지
* `개선` 아이콘을 Material 24 dp 벡터로 통일하고 비트맵 아이콘과 사용하지 않는 색상, 스타일, 문자열 리소스를 제거
* `개선` 시작 기준 프로필을 내장하여 시스템의 백그라운드 최적화 이후 인터페이스 전환으로 늘어난 홈 콜드 스타트 비용을 줄임
* `의존성` 사용자 인터페이스 이전을 위해 Jetpack Compose BOM 2026.09.00 추가
* `의존성` Compose 화면의 이미지 로딩을 위해 Glide Compose 통합 라이브러리 1.0.0-beta10 추가
* `의존성` Glide 5.0.5 -> 5.0.9 업그레이드
* `의존성` AutoJs6 Gradle Platform Versions 및 Native Alignment 플러그인 1.9.0 -> 1.9.1 업그레이드

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
