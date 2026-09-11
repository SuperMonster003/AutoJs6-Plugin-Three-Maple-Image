<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>파일 관리자 플러그인. 안전하게 이미지 편집 및 변환</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어

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

Image Tools는 파일 관리자의 단일 이미지에 독립적인 편집 및 변환 작업을 제공합니다. 원본을 변경하지 않고 읽으며 호스트 소유 출력 트랜잭션에만 씁니다.

******

### 기능

******

- 자르기, 회전, 뒤집기, 밝기, 대비, 채도, 색온도, 브러시, 텍스트 및 실행 취소로 편집합니다.
- 품질, 크기 조정, 종횡비 잠금 및 JPEG 배경을 지정해 JPEG, PNG 또는 WebP로 변환합니다.
- 원시 경로나 BitmapFactory.decodeFile 없이 ContentResolver와 ParcelFileDescriptor로 디코딩합니다.
- 호스트가 제공한 정확한 URI에만 인코딩하고 성공 시 트랜잭션 ID만 반환합니다.

******

### 지원 형식

******

확장자나 선언된 MIME을 신뢰하지 않고 Android 디코딩으로 이미지 내용을 검증합니다:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### 플러그인 인터페이스

******

호스트는 다음 식별자로 플러그인을 검색하고 실행합니다:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
```

버전 1은 기본 Explorer에만 프로토콜 v3 overflow 작업 두 개를 등록합니다. 각 작업은 읽기 전용 원본 하나를 받고 호스트 출력 트랜잭션을 통해 새 형제 파일을 만듭니다. 원본은 교체되지 않습니다.

호스트 빌드 5269 이상이 필요합니다.

******

### 보안

******

저장소 또는 네트워크 권한을 요청하지 않습니다. v3 이외의 요청, 기본 화면 이외의 요청, 예상하지 않은 작업, 상위 URI, 추가 ClipData, 쓰기 가능한 원본, content 이외 URI, 잘못된 ID, 미지원 MIME 및 256 MiB 초과 제한을 거부합니다. 출력 URI는 반환하지 않습니다.

******

### 안전 제한

******

- 작업마다 읽기 전용 입력 이미지 하나와 정확한 호스트 출력 URI 하나만 사용합니다.
- 선언된 입력 및 인코딩 출력 최대 크기: `256 MiB`.
- 출력은 JPEG, PNG 또는 WebP로 제한됩니다.
- 크기, 픽셀 수, 샘플링, 메모리, 기록 및 인코딩 바이트 수가 제한됩니다.
- 취소는 `RESULT_CANCELED`, 성공은 일치하는 트랜잭션 ID만 반환합니다.

******

### 릴리스 기록

******

# v1.0.1

###### 2026/08/08

* `수정` 플러그인 센터에서 활성화할 때 유효한 Explorer Action 서비스 바인딩 반환
* `개선` 플러그인 이름과 설명을 간결하게 하고 사용자 문서를 더 자연스럽게 정리

# v1.0.0

###### 2026/08/02

* `기능` ID `image-tools`, 작업 `edit-image` 및 `convert-image`, 엔진 `explorer-action`, 변형 `default`인 Image Tools 플러그인
* `기능` 읽기 전용 이미지 하나와 호스트 소유 create-sibling 출력 트랜잭션을 사용하는 Explorer Action v3 overflow 작업
* `기능` 자르기, 회전, 뒤집기, 색상 조정, 브러시, 텍스트 및 실행 취소를 제공하는 이미지 편집기
* `기능` 품질, 크기 조정, 비율 잠금, JPEG 배경 및 메모리 제한을 제공하는 JPEG, PNG 및 WebP 변환
* `기능` 원시 경로, 직접 형제 쓰기, 임의 URI, 저장소 권한 또는 네트워크 권한이 없는 ContentResolver 및 ParcelFileDescriptor 입출력
* `기능` 10개 언어로 현지화된 플러그인 메타데이터, 인터페이스 텍스트, 안내, README, 변경 기록
* `개선` 구성 변경 시 캔버스 도구, 대화 상자 초안, 변환 옵션, 실행 취소 기록, 진행 중 작업을 포함한 편집기와 변환기 세션 유지
* `개선` 영구 단일 사용 claim, busy guard, 취소 가능한 코루틴, writer 종료 후 결과 전달로 호스트 출력 트랜잭션 보호
* `개선` 출력 MIME 검증, 비트맵 해제, 영어 리소스 일치, 줄임표 lint 처리, 릴리스 다이제스트 스트림 종료 강화
* `의존성` 안전한 이미지 메타데이터 분석을 위해 AndroidX ExifInterface 1.4.2 추가
* `의존성` 수명 주기 및 영구 트랜잭션 테스트를 위해 Robolectric 4.16.1 추가

# v1.1.0

###### 2026/09/11

* `개선` 빌드 시 의도하지 않은 네이티브 의존성을 거부하고 JSON 보고서 생성

##### 추가 릴리스

* [CHANGELOG-ko.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-ko.md)

******

### 빌드

******

```powershell
.\gradlew.bat :app:assembleDebug
```

릴리스 빌드:

```powershell
.\gradlew.bat :app:assembleRelease
```

빌드 매개변수는 `version.properties`에서 가져옵니다. 최소 SDK는 24, 대상 SDK는 36입니다.

******

### 리소스 구성

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml`은 텍스트를 현지화합니다. `plugin_instruction.md`는 설명을 제공합니다. `.python/generate_markdown.py`는 JSON에서 README와 변경 기록을 생성합니다.

******

### 링크

******

- AutoJs6 문서: https://docs.autojs6.com
- Android 보안 파일 공유: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
