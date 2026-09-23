本目录需放入百度地图 Android SDK 的 jar/aar 文件。

请从百度地图开放平台下载 SDK：
https://lbsyun.baidu.com/index.php?title=androidsdk/guide/create-project/androidstudio

需下载以下模块（基础地图 + 定位 + 检索 + 地理围栏 + 导航）：
  - BaiduLBS_Android.jar        （定位 SDK）
  - baidumapapi_v7.5.0.jar       （基础地图 SDK）
  - baidumapapi_search_v7.5.0.jar（检索 SDK）
  - baidumapapi_util_v7.5.0.jar  （工具 SDK）
  - baidumapapi_radar_v7.5.0.jar （雷达 SDK，可选）
  - baidumapapi_navi_v7.5.0.jar  （导航 SDK）

将上述 jar 文件放入此目录，对应的 .so 文件放入 src/main/jniLibs/<abi>/ 目录。