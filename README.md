# FISH-DNSPOD

### 使用范围
在没有固定公网IP的情况下，需要使用域名绑定IP，IP每变更一次，域名解析就要手动调整一次，远程的情况下基本无解。
本项目基于腾讯DNSPOD的api，通过定时任务（每分钟）检查子域名绑定的IP是否与当前公网IP不一致，如果不一致则更新子域名。


### 使用方法
#### 1.拉取源码后，执行mvn clean package进行打包，得到fish-dnspod.jar

#### 2.编写application.properties文件

```
server.port=7101

#dns服务商，默认DNSPOD
dns.provider=DNSPOD
#dnspod api密钥，格式为"ID,Token"
dns.token=
#域名xxx.com
dns.domain=
#子域名名称，如www、kevin、app
dns.sub_domain=
```

#### 3.将fish-dnspod.jar和application.properties文件放入同一目录，执行启动命令即可

```
java -jar fish-dnspod.jar application.properties
```

#### 4.监控运行情况

```
//返回success表示服务已正常启动
http://127.0.0.1:7101/healthCheck

//返回定时任务执行情况{"exec_result":"success","last_exec_time":"2022.01.26 11:32:03"}
http://127.0.0.1:7101/stats
```



### 开机自动启动

#### 在windows系统下

##### 一、注册成服务

借用第三方工具https://github.com/winsw/winsw，可将应用注册成服务.

注意：由于winsw支持的操作系统版本有限，需要先确认当前操作系统是否满足winsw的支持范围。

亲测：windows 10可行，windows server 2008 R2 SP1不行

###### 1.下载winsw.exe

并更名为fish-dnspod.exe，放置于fish-dnspod.jar同目录

###### 2.编写fish-dnspod.xml

```
<service>
  <id>fish-dnspod</id>
  <name>fish-dnspod</name>
  <description>dynamic update dns ip</description>
  <env name="DNSPOD_HOME" value="%BASE%"/>
  <executable>java</executable>
  <arguments>-jar "%BASE%\fish-dnspod.jar" "%BASE%\application.properties"</arguments>
  <log mode="roll"></log>
  <logpath>%BASE%\win-logs\</logpath>
</service>
```

###### 3.注册fish-dnspod服务

```
fish-dnspod install
```

###### 4.启动服务

```
fish-dnspod start
```

##### 二、使用任务计划

由于javaw不支持外部配置文件，所以需要先在源码中的配置文件application.properties中修改好对应的配置再打包

###### 1.编写run.bat

可在执行脚本中指定参数

```
@echo off
copy "%JAVA_HOME%\bin\javaw.exe" "%JAVA_HOME%\bin\fish-dnspod.exe"
start fish-dnspod -jar D:\fish-dnspod\fish-dnspod.jar --server.port=7101
```

###### 2.制作windows任务计划，启动时执行run.bat



#### 二、在Linux系统下



