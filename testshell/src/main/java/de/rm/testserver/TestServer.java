package de.rm.testserver;

import de.rm.testserver.protocol.*;
import de.ruedigermoeller.serialization.FSTConfiguration;
import io.netty.channel.ChannelHandlerContext;
import org.nustaq.netty2go.NettyWSHttpServer;
import org.nustaq.webserver.WebSocketHttpServer;

import java.io.File;
import java.io.Serializable;

/**
 * Created by ruedi on 27.05.14.
 */
public class TestServer extends WebSocketHttpServer {

    FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();


    public TestServer(File contentRoot) {
        super(contentRoot);
        conf.registerCrossPlatformClassMappingUseSimpleName(new Meta().getClasses());
    }

    @Override
    public void onOpen(ChannelHandlerContext ctx) {
        sendWSBinaryMessage( ctx, conf.asByteArray(new BasicValues()) );
    }

    @Override
    public void onBinaryMessage(ChannelHandlerContext ctx, byte[] buffer) {
        Object msg = null;
        try {
            msg = conf.asObject(buffer);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        if (msg instanceof MirrorRequest) {
            sendWSBinaryMessage(ctx, conf.asByteArray((Serializable) ((MirrorRequest) msg).toMirror));
        } else {
            byte error[] = conf.asByteArray("Error");
            sendWSBinaryMessage(ctx,error);
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8887;
        }
        new NettyWSHttpServer(port, new TestServer(new File(".") )).run();
    }
}
