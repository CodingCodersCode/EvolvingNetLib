# EvolvingNetLib
网络请求库，对RxJava2+Retrofit2+OkHttp3进行了封装，达到简化网络请求的类链式调用

简介
----
EvolvingNetLib取名Evolving，意为进化，愿景是通过不断的维护，达到简化网络请求的调用和相关处理，使得开发过程能够将更多注意力放在业务方面。

EvolvingNetLib当前最新版本为v1.0.1.24。

功能
----
当前Release最新版本为v1.0.1.24版本，包含如下功能：

 - Http几种基本类型请求，包含：GET、POST、PUT、DELETE、OPTIONS、HEAD。
 - 支持Http请求的数据缓存查询和响应的数据保存回调，EvolvingNetLib不负责任何数据缓存逻辑，完全交给开发者自定义，提高灵活性。
 - 文件上传，支持上传进度回调。不支持分片和断点上传。
 - 文件下载，支持下载进度回调，支持断点下载。
 - 多线程文件下载、设置下载优先级，数值越小，优先级越高，则先下载。
 
 计划
 ----
 计划下一版本(V1.0.2)实现功能： 
 
 1.完善优先级下载功能，可动态调整优先级，目前只支持创建任务时设置优先级。 
 
 2.继续完善demo。 
 
 3.完善介绍文档 
 
 使用方式
 ----
 (1) maven 
   
          <repositories>
             <repository>
                 <id>jitpack.io</id>
                 <url>https://jitpack.io</url>
             </repository>
          </repositories>
          
          <dependency>
              <groupId>com.github.CodingCodersCode</groupId>
              <artifactId>EvolvingNetLib</artifactId>
              <version>v1.0.1.9</version>
          </dependency>
 
 (2) gradle
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
               compile 'com.github.CodingCodersCode.EvolvingNetLib:EvolvingNet:v1.0.1.9' 
             }

 - EvolvingNetLib还提供了另外一个类库，与RxLifeCycle结合，方便管理RxJava，避免内存泄漏问题的产生，若要使用，则添加如下依赖：
          
          dependencies {
            ......
            compile 'com.github.CodingCodersCode.EvolvingNetLib:EvolvingBase:v1.0.1.9'
          }
          
    EvolingBase通过结合RxLifeCycle，提供了网络请求的生命周期管理，也提供了简单的懒加载Fragment。 
    
    
  
        
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
        
请求样例
---
 - 初始化

      使用EvolvingNetLib之前需要对其进行一些初始化操作，如下代码所示:  

          //所有Http请求的公共header信息
          Map<String, String> commonHeaderMap = new HashMap<>();
          commonHeaderMap.put("common_header_param1", "common_header_value1");
          commonHeaderMap.put("common_header_param2", "common_header_value2");
          commonHeaderMap.put("common_header_param3", "common_header_value3");
          
          CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                    .baseUrl("your server host")  //服务器地址，同Retrofit的baseUrl设置，如：http://www.xxx.com/
                    .callAdapterFactory(RxJava2CallAdapterFactory.create())  //同Retrofit
                    .converterFactory(GsonConverterFactory.create())  //同Retrofit
                    .commonHeaders(commonHeaderMap)  //设置应用网络请求的公共header信息，以Map<String, String>方式传递
                    .connectTimeout(30, TimeUnit.SECONDS)  //设置写超时时间，同Retrofit的connectTimeout的含义
                    .readTimeout(30, TimeUnit.SECONDS)  //设置写超时时间，同Retrofit的readTimeout的含义
                    .writeTimeout(30, TimeUnit.SECONDS)  //设置写超时时间，同Retrofit的writeTimeout的含义
                    .enableLogInterceptor(true)  //是否开启日志打印
                    .build();  

 - 发起普通网络请求，以发起GET请求为例: 
          
          CCRxNetManager.<TestRespObj>get("your api url")  //业务api接口，例：//web/path1/path2/userinfo.do 
                       .setHeaderMap(specifyHeaderMap)  //添加的额外header信息，Map<String, String>形式传递  
                       .setPathMap(pathMap)  //restful api的路径替换信息，Map<String, String>形式传递，同Retrofit的@Path功能  
                       .setParamMap(paramMap)  //所需参数信息，Map<String, String>形式传递  
                       .setRetryCount(3)  //设置失败重试最大次数  
                       .setRetryDelayTimeMillis(3000)  //重试请求发起的时间间隔，单位毫秒  
                       .setCacheQueryMode(CCCacheMode.QueryMode.MODE_MEMORY_THEN_DISK_THEN_NET)  //数据查询策略  
                       .setCacheSaveMode(CCCacheMode.SaveMode.MODE_SAVE_MEMORY_AND_DISK)  //缓存保存策略  
                       .setReqTag("test_login_req_tag")  //请求标识  
                       .setCacheKey("test_login_req_cache_key")  //缓存查询标识  
                       .setCCNetCallback(new RxNetManagerCallback())  //请求回调，UI线程  
                       .setCCCacheSaveCallback(new RxNetCacheSaveCallback())  //数据缓存保存回调，非UI线程  
                       .setCCCacheQueryCallback(new RxNetCacheQueryCallback())  //缓存数据查询回调，非UI线程  
                       .setNetLifecycleComposer(this<CCBaseResponse<TestRespObj>>bindUntilEvent(ActivityEvent.DESTROY))  //设置请求的生命周期管理，同RxLifeCycle  
                       .setResponseBeanType(TestRespObj.class)  //响应的json数据所对应的Java bean实体类型  
                       .executeAsync();  //执行请求  
           
   其中TestRespObj.class表示服务端所响应的json数据所对应的java bean实体类的类型,若要发起其他类型的请求，只需将 CCRxNetManager.<TestRespObj>***get***("your api url") 中的 ***get*** 替换为 ***post*** 或 ***put*** 等即可。

 - 发起上传请求，示例代码如下: 
          
           CCRxNetManager.<String>upload("upload")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setTxtParamMap(txtParamMap)
                    .setFileParamMap(fileParamMap)
                    .setRetryCount(0)
                    .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                    .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                    .setReqTag("test_login_req_tag")
                    .setCacheKey("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetUploadProgressCallback())
                    .setNetLifecycleComposer(this.<CCBaseResponse<String>>bindUntilEvent(ActivityEvent.DESTROY))
                    .setResponseBeanType(TestRespObj.class)
                    .executeAsync();
 
   其中各方法含义，与普通网络请求中各方法含义相同。
     
 - 发起下载请求，示例代码如下：  
     
           CCDownloadRequest downloadRequest = CCRxNetManager.<String>download("sw-search-sp/software/16d5a98d3e034/QQ_8.9.5.22062_setup.exe")
                        .setHeaderMap(specifyHeaderMap)
                        .setPathMap(pathMap)
                        .setFileSaveName("test_OkGo_apk_file_download.apk")
                        .setRetryCount(3)
                        .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                        .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                        .setReqTag("test_login_req_tag")
                        .setCacheKey("test_login_req_cache_key")
                        .setSupportRage(true)
                        .setDeleteExistFile(false)
                        .setCCNetCallback(new RxNetDownloadCalback())
                        .setNetLifecycleComposer(this.<CCBaseResponse<String>>bindUntilEvent(ActivityEvent.DESTROY))
                        .setResponseBeanType(TestRespObj.class);

          downloadRequest.executeAsync();
     
   通过downloadRequest.executeAsync()开始或继续下载请求,
   通过downloadRequest.getNetCCCanceler().cancel()暂停或取消下载请求。
       
     
以上是对EvolvingNetLib使用方法的简单介绍，详细内容请看demo代码。

看过来
---
若在使用过程中遇到bug等问题，请提交issue，也可将你的解决方案分享出来；同样，若有好的意见、建议或修改，也请提出或发起pull request，一起完善EvolvingNetLib，一起帮助他人。

感谢
---
觉得好的，用您宝贵的小手点个start，以示对我的鼓励。不喜者勿喷，谢谢！
特别感谢[@jeasonlzy/okhttp-OkGo](https://github.com/jeasonlzy/okhttp-OkGo)和[@Tamicer/Novate](https://github.com/Tamicer/Novate),两位作者的开源库对我的工作起到了很大帮助，感谢！


License
---
Copyright 2017 CodingCodersCode

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
