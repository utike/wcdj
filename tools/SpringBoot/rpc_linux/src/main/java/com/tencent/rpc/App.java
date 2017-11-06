package com.tencent.rpc;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.tencent.demo.Demo;
import com.tencent.engine.TransMachine;
import com.tencent.engine.TransMachineManager;
import com.tencent.exception.InnerException;
import com.tencent.exception.LogicException;
// private packages
import com.tencent.midas.network.RemotingClient;
import com.tencent.midas.network.RemotingServer;
import com.tencent.midas.network.exception.RemotingConnectException;
import com.tencent.midas.network.exception.RemotingSendRequestException;
import com.tencent.midas.network.exception.RemotingTimeoutException;
import com.tencent.midas.network.netty.NettyClientConfig;
import com.tencent.midas.network.netty.NettyRemotingClient;
import com.tencent.midas.network.netty.NettyRemotingServer;
import com.tencent.midas.network.netty.NettyServerConfig;
import com.tencent.midas.network.protocol.RemotingCommand;

public class App {

	// -------------------- JNI init beg --------------------
	
	static {
		System.loadLibrary("api"); // api.dll (Windows) or libapi.so (Unixes)
	}
	// Native method declaration
	private native int init_once();
	private native int test();
	public native int process(String req);
	
	
	// -------------------- JNI init end --------------------

	// static logger object
	private static final Logger log = LoggerFactory.getLogger("RunLogger");
	public static Logger logInstance() {
		return log;
	}

	// static function, server-end
	public static RemotingServer createRemotingServer(int port)
			throws InterruptedException {

		NettyServerConfig config = new NettyServerConfig();
		config.setListenPort(port);
		RemotingServer remotingServer = new NettyRemotingServer(config);
		remotingServer.registerProcessor("ServiceTest", new ServiceTestImpl(),
				Executors.newCachedThreadPool());

		// anonymous class
		// remotingServer.registerProcessor("ServiceTest2", new
		// NettyRequestProcessor() {
		//
		// // request process
		// @Override
		// public RemotingCommand processRequest(ChannelHandlerContext ctx,
		// RemotingCommand request) {
		// request.setRemark("This is answer2." +
		// ctx.channel().remoteAddress());
		// return request;
		// }
		//
		// @Override
		// public boolean rejectRequest() {
		// return false;
		// }
		//
		// }, Executors.newCachedThreadPool());

		remotingServer.start();
		return remotingServer;
	}

	// static function, client-end
	public static RemotingClient createRemotingClient() {

		NettyClientConfig config = new NettyClientConfig();
		RemotingClient client = new NettyRemotingClient(config);
		client.start();
		return client;
	}

	/**
	 * program start
	 * 
	 * @author gerryyang
	 * @version 0.0.1
	 */
	public static void main(String[] args) {
		
		
		String path = System.getProperty("java.class.path");
		System.out.println("classpath: " + path);
		
		
		// jni
		new App().test();
		new App().init_once();  // Invoke native method
		
		try {
			// TODO register more routines
			TransMachine tm = Demo.RegistTestRoutine();
			
			TransMachineManager.instance().addTransMachine(tm);
			// ...
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("exception: create TransMachine");
		}
		
				
		// TODO
		// add conf module

		// log init, TODO config
		String logbackFile = "../conf/logback.xml";
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		lc.reset();
		try {
			configurator.doConfigure(logbackFile);
		} catch (JoranException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("load logback config[" + logbackFile + "] ok");

		try {

			// RemotingClient client = null;
			//
			RemotingServer server = createRemotingServer(8081);
			RemotingServer server2 = createRemotingServer(8082);

			log.info("server start");

			// client = createRemotingClient();
			//
			// for (int i = 0; i < 1; ++i) {
			// TestRequestHeader requestHeader = new TestRequestHeader();
			// requestHeader.setCount(i);
			// requestHeader.setMessageTitle("HelloMessageTitle");
			//
			// // TODO
			// // parse header to dispatch
			//
			// // client
			// RemotingCommand request =
			// RemotingCommand.createRequestCommand("ServiceTest",
			// requestHeader);
			// RemotingCommand response = client.invokeSync("localhost:8080",
			// request, 1000 * 3000);
			// System.out.println(i + " response[" + response.getRemark() +
			// "]");
			//
			// // write file log
			// log.info(i + "invoke result = " + response.getRemark());
			// }

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} finally {
			// if(client != null)
			// client.shutdown();
			// if(server != null)
			// server.shutdown();
		}

		System.out.println("main over");
	}
}
