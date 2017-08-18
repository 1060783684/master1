package yijiagou.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import yijiagou.handler.*;

/**
 * Created by wangwei on 17-7-28.
 */
public class PSServer {
    private int port;

    public PSServer bind(int port){
        this.port = port;
        return this;
    }

    public void run(){
        EventLoopGroup bossgroup = new NioEventLoopGroup();
        EventLoopGroup workgroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new HttpRequestDecoder());//inbound
                            channel.pipeline().addLast(new HttpObjectAggregator(65536));//inbound
                            channel.pipeline().addLast(new HttpResponseEncoder());//outbound
                            channel.pipeline().addLast(new HttpHeadHandler());//outbound
                            channel.pipeline().addLast(new HttpContentHandler());//inbound
                            channel.pipeline().addLast(new RegisterHander());//inbound outbound
                            channel.pipeline().addLast(new LoginHandler());//inbound outbound
                            channel.pipeline().addLast(new UploadHandler());//inbound outbound
                            channel.pipeline().addLast(new DownloadHander());//inbound outbound
                            channel.pipeline().addLast(new ShowAppStoreHandler());//inbound outbound
                        }
                    });
            ChannelFuture future = server.bind(port).sync();
            future.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }
    }

}
