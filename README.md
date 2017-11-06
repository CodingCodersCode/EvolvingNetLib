# EvolvingNetLib
网络请求库，对RxJava2+Retrofit2+OkHttp3进行了封装，达到简化网络请求的类链式调用

简介
----
EvolvingNetLib取名Evolving，意为进化，愿景是通过不断的维护，达到简化网络请求的调用和相关处理，使得开发过程能够将更多注意力放在业务方面。

EvolvingNetLib当前最新版本为v1.0.0。

功能
----
当前Release最新版本为v1.0.0版本，包含如下功能：
 - Http几种基本类型请求，包含：GET、POST、PUT、DELETE、OPTIONS、HEAD。
 - 支持Http请求的数据缓存查询和响应的数据保存回调，EvolvingNetLib不负责任何数据缓存逻辑，完全交给开发者自定义，提高灵活性。
 - 文件上传，支持上传进度回调。不支持分片和断点上传。
 - 文件下载，支持下载进度回调，支持断点下载。
 
 使用方式
 ----
 AndroidStudio Gradle:
  - project root `build.gradle`:
  
          allprojects {
              repositories {
                  ......
                  maven { url 'https://jitpack.io' }
                  maven { url 'https://maven.google.com' }
              }
          }
          
 - app `build.gradle`:
 
          dependencies {
            ......
            compile 'com.github.CodingCodersCode.EvolvingNetLib:EvolvingNet:v1.0.0'
          }

 - EvolvingNetLib还提供了另外一个类库，与RxLifeCycle结合，方便管理RxJava，避免内存泄漏问题的产生，若要使用，则添加如下依赖：
          
          dependencies {
            ......
            compile 'com.github.CodingCodersCode.EvolvingNetLib:EvolvingBase:v1.0.0'
          }
 - 添加和申请权限
 
        <!--网络权限-->
        <uses-permission android:name="android.permission.INTERNET" />
        <!--在SD卡中创建与删除文件权限 -->
        <uses-permission android:name="android.permission.MOUNT_FORMAT_FILESYSTEMS"/>
        <!--读取数据的权限 -->
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        <!--向SD卡写入数据的权限 -->
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        
        对于6.0+的动态权限申请，需要开发者自己实现，EvolvingNetLib不包含申请逻辑，方便开发者自定义权限申请实现方式，减少捆绑。
        
 - 请求样例
 
    - 初始化
    
      使用EvolvingNetLib之前需要对其进行一些初始化操作，如下代码所示：
      
          //所有Http请求的公共header信息
          Map<String, String> commonHeaderMap = new HashMap<>();
          commonHeaderMap.put("common_header_param1", "common_header_value1");
          commonHeaderMap.put("common_header_param2", "common_header_value2");
          commonHeaderMap.put("common_header_param3", "common_header_value3");
          
          CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                    .baseUrl("your server host")//服务器地址，同Retrofit的baseUrl设置，如：http://www.xxx.com/
                    .callAdapterFactory(RxJava2CallAdapterFactory.create())//同Retrofit
                    .converterFactory(GsonConverterFactory.create())//同Retrofit
                    .commonHeaders(commonHeaderMap)//设置应用网络请求的公共header信息，以Map<String, String>方式传递
                    .connectTimeout(30, TimeUnit.SECONDS)//设置写超时时间，同Retrofit的connectTimeout的含义
                    .readTimeout(30, TimeUnit.SECONDS)//设置写超时时间，同Retrofit的readTimeout的含义
                    .writeTimeout(30, TimeUnit.SECONDS)//设置写超时时间，同Retrofit的writeTimeout的含义
                    .enableLogInterceptor(true)//是否开启日志打印
                    .build();
                    
      发起网络请求，以发起GE请求为例：
      
          CCRxNetManager.<TestRespObj>get("/web/userController/login.do")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setParamMap(paramMap)
                    .setRetryCount(3)
                    .setRetryDelayTimeMillis(3000)
                    .setCacheQueryMode(CCCacheMode.QueryMode.MODE_MEMORY_THEN_DISK_THEN_NET)
                    .setCacheSaveMode(CCCacheMode.SaveMode.MODE_SAVE_MEMORY_AND_DISK)
                    .setReqTag("test_login_req_tag")
                    .setCacheKey("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetManagerCallback())
                    .setCCCacheSaveCallback(new RxNetCacheSaveCallback())
                    .setCCCacheQueryCallback(new RxNetCacheQueryCallback())
                    .setNetLifecycleComposer(this.<CCBaseResponse<TestRespObj>>bindUntilEvent(ActivityEvent.DESTROY))
                    .setResponseBeanType(TestRespObj.class)
                    .executeAsync();
 
 
          
