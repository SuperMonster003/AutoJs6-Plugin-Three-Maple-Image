<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>이미지 편집 및 형식 변환</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어 (Languages)

******

현재 README.md는 다음 언어를 지원합니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### 소개

******

Image Tools (이미지 도구)는 AutoJs6 파일 관리자를 위한 이미지 처리 플러그인입니다. 활성화하면 파일 관리자의 모든 이미지 파일 더보기 메뉴에 두 가지 동작이 추가됩니다: `이미지 편집`은 캔버스와 도구 모음을 갖춘 편집기를 열어 자르기, 회전, 색 보정, 낙서 같은 일상적인 수정을 처리하고, `이미지 변환`은 변환 대화상자를 열어 이미지를 JPEG, PNG 또는 WebP로 저장하며 필요하면 크기도 함께 조정합니다.

처리 결과는 항상 원본 파일 옆에 새 파일로 저장됩니다 (파일 이름에 `edited` 또는 `converted` 접미사가 붙습니다). 원본 파일은 전 과정에서 읽기 전용이며 수정, 덮어쓰기, 삭제되지 않습니다. 플러그인은 저장소 권한과 네트워크 권한을 요청하지 않으며, 호스트가 허가한 입력 파일 하나와 출력 위치 하나에만 접근할 수 있습니다.

******

### 주요 기능

******

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

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="파일 메뉴 작업" width="300" />
      <br />
      <sub>파일 메뉴 작업</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="이미지 편집기" width="300" />
      <br />
      <sub>이미지 편집기</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="JPEG 변환 옵션" width="300" />
      <br />
      <sub>JPEG 변환 옵션</sub>
    </td>
  </tr>
</table>

******

### 설치 및 사용

******

시작하기 전에 다음 요구 사항을 확인하세요:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

설치부터 첫 이미지 처리까지 4단계입니다:

1. 플러그인 APK를 다운로드하여 설치합니다. 플러그인에는 런처 아이콘이 없으며 설치 후 AutoJs6가 전적으로 관리합니다.
2. AutoJs6를 열고 `플러그인 센터`에 들어가 `이미지 도구`를 찾아 활성화합니다.
3. AutoJs6 파일 관리자에서 아무 이미지 파일 (예: `photo.jpg`)을 찾아 해당 파일의 더보기 메뉴를 엽니다.
4. `이미지 편집`을 선택해 편집기로 들어가거나, `이미지 변환`을 선택해 변환 대화상자를 엽니다.

편집기 도구 모음에는 `자르기`, `왼쪽으로 회전`, `오른쪽으로 회전`, `미세 회전`, `좌우 뒤집기`, `상하 뒤집기`, `밝기`, `대비`, `채도`, `색온도`, `브러시`, `텍스트`가 있습니다. 자르기는 자유, 고정, 원본 비율 사전 설정을 제공하고 미세 회전은 -45°~+45°입니다. 브러시는 펜, 형광펜, 모자이크, 지우개이며 텍스트는 여러 줄, 윤곽선과 그림자를 지원합니다. 상단에는 `실행 취소`, `다시 실행`, `저장`, 추가 메뉴에는 `원본 복원`이 있습니다. `저장`은 원본 형식을 따르거나 JPEG / PNG / WebP를 선택하고 손실 품질을 정하며 Android 11+에서 무손실 WebP를 켜는 대화상자를 엽니다. 호스트는 `edited` 접미사의 새 같은 위치 파일을 게시합니다. 원본 메타데이터는 제거되고 원본 파일은 덮어쓰지 않습니다.

변환 대화상자는 JPEG / PNG / WebP (기본 PNG), 품질 1-100 (기본 92), Android 11+ 무손실 WebP, JPEG 또는 손실 WebP의 품질을 자동 선택하는 `목표 파일 크기`를 제공합니다. `크기 조정`은 `원본`, `백분율` (1-1000), 기본 1920 px이며 확대하지 않는 `긴 변`, 비율 고정을 선택할 수 있는 `사용자 지정`입니다. JPEG는 투명 영역을 흰색이나 검정으로 채우고 PNG는 결과가 256색 이하이면 자동으로 인덱스 팔레트를 사용합니다. `안전한 EXIF 메타데이터 유지`는 기본적으로 꺼져 있으며 GPS, 포함된 미리 보기와 안전하게 검사할 수 없는 메타데이터를 항상 제거합니다. 대화상자는 해상도, 예상 크기와 접미사를 표시합니다. `변환`은 호스트에 `converted` 접미사의 같은 위치 파일 게시를 요청하고 `취소`나 뒤로는 파일을 만들지 않습니다.

******

### 지원 형식

******

파일 관리자는 다음 확장자 (및 모든 `image/*` MIME 유형)의 파일에 플러그인 동작을 표시합니다:

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

플러그인은 파일 내용으로 이미지를 식별하며 확장자를 신뢰하지 않습니다: 열 수 있는지는 Android가 해당 파일을 디코딩할 수 있는지에 달려 있습니다. GIF와 움직이는 WebP 같은 애니메이션 파일은 첫 프레임만 처리합니다. 입력과 출력 파일의 크기 상한은 각각 256 MiB입니다.

******

### 자주 묻는 질문

******

**파일 메뉴에 `이미지 편집`과 `이미지 변환`이 보이지 않나요?**

다음 순서로 확인하세요: AutoJs6 버전 코드가 5269 이상인지; 플러그인이 `플러그인 센터`에서 활성화되어 있는지; 파일 확장자나 MIME 유형이 지원 목록에 있는지. 셋 중 하나라도 충족하지 않으면 메뉴 동작이 나타나지 않습니다.

**열 때 `이미지 정보를 읽을 수 없습니다`라고 표시되거나 화면이 바로 닫히나요?**

흔한 원인: 파일이 손상되었거나 실제 이미지가 아님 (플러그인은 내용으로 판별하므로 확장자만 바꿔도 소용없음); 시스템이 해당 형식을 디코딩할 수 없음 (HEIC / HEIF는 일반적으로 Android 9 미만에서 지원되지 않음); 파일이 256 MiB를 초과함; 호출이 AutoJs6 파일 관리자에서 오지 않음. 보안상의 이유로 플러그인은 다른 출처의 호출을 거부합니다.

**처리 결과는 어디에 저장되나요? 원본을 덮어쓰나요?**

절대 덮어쓰지 않습니다. 결과는 호스트가 `edited` 또는 `converted` 접미사가 붙은 새 파일로 원본 옆에 게시하며, 파일 이름 충돌도 호스트가 자동으로 피합니다. 원본 파일은 플러그인에게 전 과정 읽기 전용입니다.

**큰 이미지를 변환할 때 메모리 부족이나 픽셀 수 초과 경고가 나오나요?**

출력 크기에는 세 가지 제한이 있습니다: 한 변이 16384 px를 넘을 수 없고, 총 픽셀 수가 4천만 (40 MP)을 넘을 수 없으며, 기기 메모리 예산 안에 들어야 합니다. 원본이 제한을 넘는 경우 `크기 조정`을 `백분율`, `긴 변` 또는 `사용자 지정`으로 바꿔 출력을 줄이세요. 메모리 부족은 다른 앱을 종료하거나 해상도를 더 낮추면 대개 해결됩니다.

**편집 후 저장한 이미지의 해상도가 왜 낮아졌나요?**

편집을 부드럽고 안정적으로 유지하기 위해, 편집 픽셀 예산 (기기 메모리에 따라 최대 약 16 MP)을 넘는 이미지는 다운샘플링을 거쳐 편집기에 올라가며, 저장 결과는 편집 캔버스의 해상도와 같습니다. 픽셀을 건드리지 않고 형식이나 크기만 바꾸려면 `이미지 변환`을 사용하세요. 출력 크기에 맞춰 정밀하게 디코딩하므로 이 예산의 제한을 받지 않습니다.

**여러 이미지를 한 번에 처리하거나 결과를 다른 디렉터리에 저장할 수 있나요?**

아직 안 됩니다. explorer-action 프로토콜 v3는 단일 파일 동작과 형제 파일 출력만 지원하며, 플러그인이 출력 위치를 스스로 고를 수도 없습니다. 다중 선택 동작과 추가 출력 방식은 프로토콜의 이후 버전에 달려 있으며 로드맵에서 추적합니다.

******

### 보안

******

플러그인은 기본 거부 원칙으로 만들어졌습니다. 다음 조치는 모두 항상 켜져 있으며 끌 수 없습니다:

- 원본 파일은 엄격한 읽기 전용입니다: 플러그인은 호스트가 부여한 일회용 읽기 전용 content URI로만 입력을 열고, 파일 시스템 경로를 받지 않으며, 저장소나 네트워크 권한을 요청하지 않습니다.
- 출력은 호스트가 미리 만들어 둔 정확한 출력 위치에만 기록되고, 성공 시 플러그인은 트랜잭션 ID만 반환합니다. 다른 URI를 선택, 생성, 반환할 수 없습니다.
- 모든 출력 트랜잭션은 일회용입니다: 사용된 트랜잭션 ID는 영구 기록되며, 재전송되거나 중복된 요청은 즉시 거부됩니다.
- 모든 호출은 완전하게 검증됩니다: 프로토콜 버전, 호출 출처, 동작 ID, 권한 모드, MIME 유형, 표시 이름, 트랜잭션 ID 중 하나라도 일치하지 않으면 실행을 중단하고, 원본에 대한 쓰기 권한 부여 역시 거부합니다.
- 입력과 출력은 각각 256 MiB로 제한되고, 출력 해상도는 한 변 16384 px와 총 40 MP를 넘을 수 없으며, 인코딩 바이트 수는 쓰는 동안 실시간으로 상한이 적용됩니다.
- 새로 인코딩한 출력은 원본 메타데이터를 기본적으로 제거합니다. 선택적인 안전한 EXIF 유지는 한도가 있는 허용 목록을 사용하며, 방향은 정규화하고 GPS 위치, 포함된 미리보기 및 안전하게 검사할 수 없는 메타데이터는 항상 제거합니다.

******

### 플러그인 인터페이스 (개발자용)

******

호스트는 다음 식별 정보로 플러그인을 발견하고 호출합니다:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
explorer action ids: edit-image / convert-image
input MIME types: image/*
output MIME types: image/jpeg, image/png, image/webp
required host build: 5269
```

현재 구현은 explorer-action 프로토콜 v3를 대상으로 합니다: 파일 관리자 메인 화면에서 단일 이미지 파일에 두 개의 더보기 메뉴 동작을 제공하고, 각 동작은 읽기 전용 입력 하나를 받아 호스트 소유의 create-sibling 출력 트랜잭션으로 새 파일 하나를 기록하며, 성공 시 트랜잭션 ID만 반환합니다. 다중 선택과 디렉터리 수준 동작은 프로토콜의 이후 버전에 달려 있으며 로드맵에서 추적합니다.

******

### 로드맵

******

완료된 기능과 향후 계획은 체크 가능한 목록으로 ROADMAP.md에서 관리합니다. 체크되지 않은 항목은 의향을 나타내며 현재 기능을 설명하지 않습니다.

- [체크 가능한 ROADMAP.md 열기](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### 릴리스 기록

******

#### v1.2.1

###### 2026/09/19

* `수정` 공유 빌드 플러그인 1.8.3을 통해 AGP 9.1의 SDK XML v4 파싱 경고 및 JVM 단위 테스트 조립 작업에서 APK 네이티브 라이브러리 정렬 검사가 잘못 실행되는 문제 해결
* `개선` compileSdk 와 targetSdk 를 37 (Android 17) 로 올리며, 플러그인 동작은 새 대상 버전의 영향을 받지 않음

#### v1.2.0

###### 2026/09/13

* `기능` 화면에서 현지화된 로컬 릴리스 기록을 표시하고 영어 대체 제공
* `개선` 릴리스 서명 설정, 예상 APK 구성 및 문서 재생성 결과 검증

#### v1.1.0

###### 2026/09/12

* `기능` 대칭 실행 취소 / 다시 실행, 되돌릴 수 있는 원본 복원, 자르기 비율 프리셋, 출력 크기를 바꾸지 않는 -45°~+45° 미세 회전을 추가
* `기능` 펜, 형광펜, 모자이크, 지우개 도구와 윤곽선, 그림자, 회전을 지원하는 드래그 가능한 여러 줄 텍스트를 추가
* `기능` 편집기에 원본 형식 / JPEG / PNG / WebP 저장 선택, 조절 가능한 손실 품질, Android 11+ 무손실 WebP를 추가
* `기능` 변환기에 무손실 WebP, 최대 256색 이미지용 인덱스 PNG, JPEG / 손실 WebP 목표 파일 크기, 확대하지 않는 긴 변 크기 조절을 추가
* `기능` 안전한 EXIF 선택 보존을 추가하고 GPS, 내장 미리보기, 불투명 메타데이터는 항상 제거하며 방향을 정규화
* `수정` BitmapFactory의 경계 전용 검사가 올바르게 비트맵을 반환하지 않을 때 유효한 이미지가 거부되던 문제를 수정
* `수정` 어두운 모드에서 편집기 도구 레이블을 읽을 수 없던 문제를 수정
* `개선` 상태 복원, 메모리 / 출력 제한 적용, 회귀 검증을 23개 스위트 / 99개 테스트로 확대
* `개선` 공유 소스에서 10개 언어 README와 호스트 설명을 갱신하고 개인 데이터가 없는 실제 기기 스크린샷 3장을 추가
* `개선` 빌드 시 의도하지 않은 네이티브 의존성을 거부하고 JSON 보고서 생성

##### 전체 기록

* [CHANGELOG-ko.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-ko.md)

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

빌드 매개변수는 `version.properties`에서 가져옵니다. 현재 최소 SDK는 24이고 대상 SDK는 36입니다.

******

### 리소스 구조

******

```text
.readme/lang_*.json
.readme/template_plugin_instruction.md
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml`은 플러그인 정보와 편집기 및 변환기 UI를 현지화합니다. README, CHANGELOG와 호스트 측 `plugin_instruction.md`는 모두 `.python/generate_markdown.py`가 JSON 소스와 Markdown 템플릿에서 생성합니다: 문서를 수정할 때는 `.readme`와 `.changelog` 아래의 소스를 편집한 뒤 스크립트를 다시 실행하고, 생성된 Markdown 파일을 직접 편집하지 마세요.

******

### 링크

******

- AutoJs6 문서: https://docs.autojs6.com
- Android 안전한 파일 공유: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
