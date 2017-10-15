package com.uncc.socket;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class SocketServer {

	/**
	 * @param args
	 */
	private static int port = 8080; 
	private StringBuffer incomingMsg;
	private static long packetNumber;
	private static long threadNumber; 
	private String response;
	private final static String noAck = "No ACK";
	
	
		  private static final Logger logger = Logger.getLogger("myLogger");
		    private Channel ch;

		    public void run() throws Exception {
		        final EventLoopGroup bossGroup = new NioEventLoopGroup();
		        final EventLoopGroup workerGroup = new NioEventLoopGroup();
		        try {
		            final ServerBootstrap b = new ServerBootstrap();
		            b.group(bossGroup, workerGroup)
		                    .channel(NioServerSocketChannel.class)
		                    .childHandler(new MySocketInitialiser())
		                    .option(ChannelOption.SO_BACKLOG, 100)
		                    .childOption(ChannelOption.SO_KEEPALIVE, true);
		            String hostname = getHostName();
		            ch = b.bind(hostname, port).sync().channel();
		            System.out.println("Started on port : server "+ port + ":"+hostname);
//		            logger.log(Level.INFO, "Started on port : server "+ port + ":"+hostname);
		            ch.closeFuture().sync();
		        } finally {
		            bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();
		        }
		    }

		    public static void main(String[] args) throws Exception {
		        new SocketServer().run();
//		        logger.log(Level.INFO,"Inside main method()");
		        System.out.println("Inside main method()");
		    }

		    private class MySocketInitialiser extends ChannelInitializer<SocketChannel> {
		        @Override
		        public void initChannel(final SocketChannel ch) throws Exception {
		            final ChannelPipeline pipeline = ch.pipeline();
		            
		            /**
		            pipeline.addLast(LineBasedFrameDecoder.class.getName(),new LineBasedFrameDecoder(256));
		            pipeline.addLast(StringDecoder.class.getName(), new StringDecoder(CharsetUtil.UTF_8));
		            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		            pipeline.addLast("decoder", new StringDecoder());
		            pipeline.addLast("encoder", new StringEncoder());
		            **/
		            final EventExecutorGroup group = new DefaultEventExecutorGroup(20);
		            pipeline.addLast(group, "handler", new SomeHandler());
		            ++threadNumber;
		            System.out.println("Exitting initchannel method()");
//		            logger.log(Level.INFO,threadNumber+"::Exitting initchannel method()");
		        }
		    }

		    public class SomeHandler extends ChannelInboundHandlerAdapter {
		        @Override
				public void channelRead(ChannelHandlerContext channelHandlerContext, Object notification) throws Exception {
		        	 // Read the message sent from client.
		        		++packetNumber;
		                ByteBuf in = (ByteBuf) notification;
		                incomingMsg = new StringBuffer();
		                try {
		                    while (in.isReadable()) { // (1)
		                    	incomingMsg.append((char) in.readByte());
		                    }
		                    System.out.println(packetNumber+"::CCS message:"+incomingMsg);
//		                    logger.log(Level.INFO,packetNumber+"::CCS message:"+incomingMsg);
		                    InvokeWS obj = new InvokeWS();
		                    response = obj.sendGet(incomingMsg.toString(), packetNumber);
//				            logger.log(Level.INFO,"Going to write response:"+response);
				            System.out.println("Going to write response:"+response);
				            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
		                }
		                catch(Exception e){
//		                    logger.log(Level.SEVERE,"---------------- Reading Exception ----------------");
		                    System.out.println("---------------- Reading Exception ----------------");
		                    channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(noAck, CharsetUtil.UTF_8));
		                    System.out.println(e.getMessage());
		                }finally{
		                    ReferenceCountUtil.release(channelHandlerContext);
		                }
		            
		        }
		        
		        
		        @Override
		        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		          if (cause instanceof ReadTimeoutException) {
		            System.out.println("Read timeout - close connection");
		          } else {
		        	  System.out.println(cause.getMessage());
		          }
		          ctx.channel().close();
		          ctx.channel().pipeline().remove(this);
		        }
		        
		        
		    }
		    
		    
		    
		    protected static String getHostName()
		    {
		    	
		        Map<String, String> env = System.getenv();
		        if (env.containsKey("HOSTNAME"))
		            return env.get("HOSTNAME") + "server_name";
				return null;
		    }
		    
		  

		    
		}