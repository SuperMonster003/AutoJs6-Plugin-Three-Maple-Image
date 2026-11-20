{{ instruction_p_what }}

### {{ instruction_h3_start }}

{{ placeholder_instruction_start_steps }}

### {{ instruction_h3_features }}

{{ placeholder_instruction_features }}

### {{ instruction_h3_script }}

{{ instruction_p_script }}

```js
let images = gallery.pick({ multiple: true, maxCount: 5 });
if (images) {
    let outputs = images.map((image) => gallery.convert(image.path, { format: 'webp', longEdge: 1080 }).path);
    gallery.view(outputs);
}
```

{{ placeholder_instruction_script_notes }}

### {{ instruction_h3_requirements }}

{{ placeholder_instruction_requirements }}

### {{ instruction_h3_faq }}

{{ placeholder_instruction_faq }}

{{ instruction_p_more }}
