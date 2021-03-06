package com.tencent.rpc;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

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

	// static logger object
	private static final Logger log = LoggerFactory.getLogger("RPC");

	// static function, server-end
	public static RemotingServer createRemotingServer() throws InterruptedException {

		NettyServerConfig config = new NettyServerConfig();
		config.setListenPort(8080);
		RemotingServer remotingServer = new NettyRemotingServer(config);
		remotingServer.registerProcessor("ServiceTest", new ServiceTestImpl(), Executors.newCachedThreadPool());

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
	 * @author gerryyang
	 * @version 0.0.1
	 */
	public static void main(String[] args) {
		
		// TODO
		// add conf module
		
		// log init, TODO config
		String logbackFile = "conf/logback.xml";
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
		
		RemotingServer server = null;;
		RemotingClient client = null;
		try {
			server = createRemotingServer();
			client = createRemotingClient();
			
			for (int i = 0; i < 3; ++i) {
				TestRequestHeader requestHeader = new TestRequestHeader();
				requestHeader.setCount(i);
				requestHeader.setMessageTitle("HelloMessageTitle");

				// TODO
				// parse header to dispatch

				// client
				RemotingCommand request = RemotingCommand.createRequestCommand("ServiceTest", requestHeader);
				RemotingCommand response = client.invokeSync("localhost:8080", request, 1000 * 3000);
				System.out.println(i + " response[" + response.getRemark() + "]");

				// write file log
				log.info(i + "invoke result = " + response.getRemark());
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} finally {
			if(client != null)
				client.shutdown();
			if(server != null)
				server.shutdown();
		}
		
		System.out.println("main over");
	}
}
