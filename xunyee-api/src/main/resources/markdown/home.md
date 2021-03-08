### 寻艺2.0接口文档

1. 全局响应参数说明

   > 所有接口返回格式都是这个，如果接口有返回参数，则参数会放在 "data" 中。

   ````java 
   {
   	"code": 0,
   	"data": {},
   	"msg": ""
   }
   ````

   | 名称 | 释意                 | 示例           |
      | ---- | -------------------- | -------------- |
   | code | 错误码               | 0 成功，-1失败 |
   | msg  | 错误文字             |                |
   | data | 返回参数包含在data中 |                |

2. 分页说明

   > 分页传参需要两个字段
   >
   > current : 页码
   >
   > size ：每次请求多少条

   ```java
   {
   	"code": 0,
   	"data": {
   		"current": 0,
   		"hitCount": true,
   		"pages": 0,
   		"records": [
   			{
   				"avatar": "",
   				"cover": "",
   				"id": 0,
   				"is_star": true,
   				"nickname": "",
   				"star_count": 0,
   				"title": "",
   				"vcuser_d": 0
   			}
   		],
   		"searchCount": true,
   		"size": 0,
   		"total": 0
   	},
   	"msg": ""
   }
   ```

   | 名称    | 释意       | 示例                                                         |
      | ------- | ---------- | ------------------------------------------------------------ |
   | current | 当前页码   | 1                                                            |
   | pages   | 总页数     | 10                                                           |
   | size    | 每页多少条 | 20                                                           |
   | total   | 总共多少条 | 200                                                          |
   | records | 返回的数据 | {<br/>				"avatar": "",<br/>				"cover": "",<br/>				"id": 0,<br/>				"is_star": true,<br/>				"nickname": "",<br/>				"star_count": 0,<br/>				"title": "",<br/>				"vcuser_d": 0<br/>			} |

   