# challenge
challenge

赛题链接: [【赛道1】实现一个分布式统计和过滤的链路追踪](https://tianchi.aliyun.com/competition/entrance/231790/information) https://tianchi.aliyun.com/competition/entrance/231790/information

感谢阅读我的代码，整体主要分为两个部分，

1、filter负责获取日志、建立索引、发现错误，

2、engine负责协调两个filter同步、去另外一个节点拉去错误的日志、计算hash

filter和engine之间的通信是自己使用tcp实现的，其中的packet是就是自己定义的通信协议

获取日志：主要就是去节点拉去日志，然后放入page中，

建立索引：就是根据链路id和记录行的开始/结束建立，这里一般情况使用java自带的HashMap，但是为了提前分配好内存和提高速度，所以自己使用三维数组实现了一个HashMap，这里可能是比较难看的，代码的可读性很差，没有做封装其目的都是为了提高速。

发现错误：这里和发现行是混在一起处理的，其目的就是为了不做第二次遍历，发现行就是找到换行符，发现错误就是找到error和状态不为200的，在查找错误的时候会做一定的跳跃，其目的就是减少遍历的次数。

整体结构是这样，希望对你有点帮助
