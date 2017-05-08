package com.yaowan.framework.druid;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.LogUtil;

public class JettyServer{  
	
    public static void start(){  
    	 try {  
             // 服务器的监听端口  
             Server server = new Server(DruidControllerSystemConfig.jettyPort);  
             // 关联一个已经存在的上下文  
             WebAppContext context = new WebAppContext();  
             // 设置描述符位置  
             context.setDescriptor(DruidControllerSystemConfig.descriptor);  
             // 设置Web内容上下文路径  
             context.setResourceBase(DruidControllerSystemConfig.resourceBase);  
             // 设置上下文路径  
             context.setContextPath(DruidControllerSystemConfig.contextPath);  
             context.setParentLoaderPriority(true);  
             server.setHandler(context);  
             // 启动  
             server.start();  
         	LogUtil.info("阿里连接池的监控系统端口启动完毕！端口="+ DruidControllerSystemConfig.jettyPort);
           //  server.join();  
         } catch (Exception e) {  
             e.printStackTrace();  
         }  
    }
}  
