<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>파일 관리자 플러그인. 확대, 메타데이터, 공유 및 안전한 외부 대체 기능으로 이미지 보기</p>

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

Image Viewer는 파일 관리자에서 지원되는 이미지를 여는 기본 작업을 제공합니다. 임시 읽기 전용 content URI를 전용 뷰어에서 열며 원본 파일을 변경하지 않습니다.

******

### 기능

******

- 호스트가 이전에 표시하던 8개 이미지 확장자에 대해 프로토콜 v2 탐색기 기본 작업을 등록합니다.
- 이미지를 화면에 맞추고 초점 핀치 확대, 이동, 두 번 탭하여 재설정, 탭하여 컨트롤 숨기기를 지원합니다.
- 파일 이름, MIME 유형, 크기 및 디코딩된 해상도를 표시합니다.
- 이미지를 공유하거나 이 플러그인 자체를 제외한 다른 호환 앱으로 엽니다.
- 읽기 전용 `content` URI와 `image/*` MIME 유형을 위한 독립 Android `ACTION_VIEW` 게이트웨이를 제공합니다.

******

### 지원 형식

******

탐색기 기본 작업은 다음 확장자와 정확히 일치합니다:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### 플러그인 인터페이스

******

호스트는 다음 식별자로 플러그인을 검색하고 실행합니다:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
```

버전 1은 파일 관리자에서 이미지 기본 작업을 제공합니다. 이미지 편집, 변환, 파일 정보, 삭제, 이동 및 이름 바꾸기는 호스트 기능으로 유지됩니다. 플러그인이 없으면 호스트는 읽기 전용 외부 `ACTION_VIEW` 요청으로 대체합니다.

호스트 빌드 5269 이상이 필요합니다.

******

### 보안

******

플러그인은 저장소 또는 네트워크 권한을 요청하지 않습니다. 호스트는 대상 content URI에 임시 읽기 전용 접근만 부여합니다. 탐색기 게이트웨이는 정확한 작업, URI, ClipData, 파일 이름, MIME 유형, 선언된 크기 및 상위 디렉터리 관계를 검증하고 쓰기 또는 영구 권한을 거부하며 원본을 쓰지 않습니다. 외부 `ACTION_VIEW` 게이트웨이는 분리되어 있으며 읽기 전용 이미지 `content` URI만 수락하고 검증된 대상만 전달합니다.

******

### 안전 제한

******

- 최대 입력 크기: `8 TiB`.
- 작업당 대상 파일 1개.
- 탐색기 카탈로그: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- 외부 `ACTION_VIEW`: `image/*` MIME 유형의 읽기 전용 `content` URI.
- 실제 디코딩 지원은 Android와 Glide에도 의존합니다.
- 이미지 편집, 변환, 삭제, 이동 및 이름 바꾸기는 이 플러그인 범위 밖입니다.

******

### 릴리스 기록

******

# v1.0.1

###### 2026/08/08

* `수정` 플러그인 센터 활성화를 막던 null 서비스 바인딩
* `개선` 더 간결한 플러그인 이름, 설명 및 사용자 문서

# v1.0.0

###### 2026/08/02

* `기능` 플러그인 ID `image-viewer`, 작업 ID `view-image`, 엔진 `explorer-action`, 변형 `default`의 Image Viewer 플러그인
* `기능` BMP, GIF, JFIF, JPE, JPEG, JPG, PNG 및 WEBP 파일을 위한 Explorer Action 프로토콜 v2 기본 이미지 보기
* `기능` 화면 맞춤 표시, 초점 핀치 확대, 이동, 두 번 탭하여 재설정 및 탭하여 컨트롤 숨기기
* `기능` 파일 이름, MIME 유형, 크기 및 디코딩된 해상도 메타데이터와 공유 및 안전한 외부 뷰어 대체
* `기능` 임시 읽기 전용 URI 접근과 8 TiB 입력 제한을 갖춘 보호된 탐색기와 공개 Android `ACTION_VIEW`용 분리 게이트웨이
* `기능` 호스트 빌드 5269 이상 요구 사항
* `기능` 스페인어, 프랑스어, 러시아어, 아랍어, 일본어, 한국어, 영어, 중국어 간체, 홍콩 중국어 번체 및 대만 중국어 번체로 현지화한 메타데이터, 인터페이스, 사용 안내, README 및 변경 기록
* `의존성` Glide 버전 5.0.5 추가

# v1.2.0

###### 2026/09/11

* `개선` 빌드 시 의도하지 않은 네이티브 의존성을 거부하고 JSON 보고서 생성

##### 더 많은 릴리스

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

빌드 매개변수는 `version.properties`에서 가져옵니다. 현재 최소 SDK는 24이고 대상 SDK는 36입니다.

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

`strings.xml`은 플러그인 메타데이터와 UI 텍스트를 현지화합니다. `plugin_instruction.md`는 호스트에 표시되는 안내를 제공합니다. `.python/generate_markdown.py`는 JSON 원본에서 현지화된 README와 변경 기록을 생성합니다.

******

### 링크

******

- AutoJs6 문서: https://docs.autojs6.com
- Android 보안 파일 공유: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
