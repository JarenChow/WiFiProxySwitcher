# WiFi Proxy Switcher

 ![image](https://github.com/M3oM3oBug/WiFiProxySwitcher/raw/master/img1.png)

 ![image](https://github.com/M3oM3oBug/WiFiProxySwitcher/raw/master/img2.png)

# 功能
* 一键增加 WLAN 高级设置中的 Http Proxy 代理设置
* 一键取消此代理设置
* 兼容 4.0 ~ 6.0 系统

# 注意事项
* 6.0 系统需要在系统设置断开 WiFi, 单独在软件中连接 WiFi
* 需要配合局域网代理主机使用

# 典型使用情形
* 电脑开着, 躺床上想用手机共享到电脑上的 Proxy 流量, 于是, 清爽一键开启, 达成心愿

# 原理
## WLAN -> Long Click -> Modify network -> Show advanced options -> Proxy -> Manual -> Proxy hostname/Proxy port, 可以使用到局域网中的 http 代理或者 socks5 代理.
## 利用 Java 中的反射, 将上面这一大堆操作简化为按一下按钮的指令
