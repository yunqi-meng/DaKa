本目录需放入百度地图 SDK 的 .so 动态库文件，按 CPU 架构分目录：

jniLibs/
├── armeabi-v7a/
│   ├── libBaiduMapSDK_base_v7.5.0.so
│   ├── libBaiduMapSDK_map_v7.5.0.so
│   ├── liblocSDK8.so
│   └── ...
├── arm64-v8a/
│   └── ...
├── x86/
│   └── ...
└── x86_64/
    └── ...

.so 文件随 SDK 压缩包一同提供，解压后按架构放入对应子目录。