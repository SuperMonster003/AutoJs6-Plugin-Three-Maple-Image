<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="{{ repo_url }}/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="{{ icon_alt }}" border="0" width="128" />
  </p>

  <p>{{ text_plugin_synopsis }}</p>

  <p>
    <a href="{{ repo_url }}/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/{{ repo_slug }}?label=Release"/></a>
    <a href="{{ repo_url }}/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/{{ repo_slug }}?color=A24232&label=Issues"/></a>
    <a href="{{ repo_url }}/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/{{ repo_slug }}?color=534BAE&label=License"/></a>
  </p>
</div>

******

### {{ h3_languages_with_ascii }}

******

{{ p_languages_all_supported_for_readme }}:

{{ placeholder_ul_languages_all_supported }}

******

### {{ h3_introduction }}

******

{{ p_introduction }}

{{ p_introduction_secure }}

******

### {{ h3_functions }}

******

{{ placeholder_highlights }}

******

### {{ h3_screenshots }}

******

{{ p_screenshots_intro }}

<table>
  <tr>
    <td align="center">
      <img src="{{ repo_url }}/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="{{ screenshot_explorer_caption }}" width="360" />
      <br />
      <sub>{{ screenshot_explorer_caption }}</sub>
    </td>
    <td align="center">
      <img src="{{ repo_url }}/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="{{ screenshot_selection_caption }}" width="360" />
      <br />
      <sub>{{ screenshot_selection_caption }}</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="{{ repo_url }}/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="{{ screenshot_viewer_caption }}" width="360" />
      <br />
      <sub>{{ screenshot_viewer_caption }}</sub>
    </td>
    <td align="center">
      <img src="{{ repo_url }}/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="{{ screenshot_zoom_caption }}" width="360" />
      <br />
      <sub>{{ screenshot_zoom_caption }}</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="{{ repo_url }}/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="{{ screenshot_share_caption }}" width="360" />
      <br />
      <sub>{{ screenshot_share_caption }}</sub>
    </td>
  </tr>
</table>

******

### {{ h3_usage }}

******

{{ p_usage_prerequisites }}:

```text
host app: AutoJs6 ({{ host_package }})
minimum host build: {{ required_host_build }}
minimum android: {{ min_android }}
plugin package: {{ plugin_package }}
```

{{ p_usage_steps_intro }}:

{{ placeholder_usage_steps }}

{{ p_usage_viewer_tips }}

******

### {{ h3_supported_formats }}

******

{{ p_supported_formats }}:

```text
{{ supported_formats }}
```

{{ p_supported_formats_note }}

******

### {{ h3_faq }}

******

{{ placeholder_faq }}

******

### {{ h3_security }}

******

{{ p_security_intro }}:

{{ placeholder_security_points }}

******

### {{ h3_plugin_interface }}

******

{{ p_plugin_interface }}:

```text
service action: {{ plugin_action }}
execute action: {{ plugin_execute_action }}
plugin id: {{ plugin_id }}
engine: {{ plugin_engine }}
variant: {{ plugin_variant }}
explorer action id: {{ explorer_action_id }}
protocol version: {{ protocol_version }}
MIME type: {{ mime_type }}
required host build: {{ required_host_build }}
```

{{ p_plugin_scope }}

******

### {{ h3_roadmap }}

******

{{ p_roadmap_status }}

- [{{ text_open_roadmap }}]({{ repo_url }}/blob/master/ROADMAP.md)

******

### {{ h3_release_history }}

******

{{ placeholder_latest_release_history }}

##### {{ h5_for_more_release_history }}

* {{ placeholder_read_more_in_changelog_md }}

******

### {{ h3_build }}

******

```powershell
.\gradlew.bat :app:assembleDebug
```

{{ text_release_build }}:

```powershell
.\gradlew.bat :app:assembleRelease
```

{{ p_build_params }}.

******

### {{ h3_resource_layout }}

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

{{ p_resource_layout }}.

******

### {{ h3_links }}

******

- {{ text_link_autojs6_docs }}: {{ docs_autojs6_url }}
- {{ text_link_android_secure_file_sharing }}: {{ android_secure_files_url }}
- {{ text_link_glide }}: {{ glide_repo_url }}


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
