package ai.grakn.redismock;

import ai.grakn.redismock.commands.RedisOperationExecutor;
import ai.grakn.redismock.expecptions.EOFException;
import ai.grakn.redismock.expecptions.ParseErrorException;
import com.google.common.base.Preconditions;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

;

/**
 * Created by Xiaolu on 2015/4/18.
 */
public class RedisClient implements Runnable {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RedisClient.class);
    private final RedisOperationExecutor executor;
    private final Socket socket;
    private final ServiceOptions options;
    private final InputStream in;
    private final OutputStream out;

    public RedisClient(RedisBase base, Socket socket, ServiceOptions options) throws IOException {
        Preconditions.checkNotNull(base);
        Preconditions.checkNotNull(socket);
        Preconditions.checkNotNull(options);

        this.executor = new RedisOperationExecutor(base, this);
        this.socket = socket;
        this.options = options;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    public void run() {
        int count = 0;
        while (true) {
            try {
                RedisCommand command = RedisCommandParser.parse(in);
                Slice response = executor.execCommand(command);
                sendResponse(response);

                count++;
                if (options.getCloseSocketAfterSeveralCommands() != 0
                        && options.getCloseSocketAfterSeveralCommands() == count) {
                    break;
                }
            } catch (EOFException | IOException e) {
                LOG.error("Internal error", e);
                // Do nothing
                break;
            } catch (ParseErrorException e) {
                // TODO return error
            }
        }
    }

    public void sendResponse(Slice response) throws IOException {
        if(!response.equals(Response.SKIP)) {
            out.write(response.data());
            out.flush();
        }
    }

    public void close(){
        Utils.closeQuietly(socket);
        Utils.closeQuietly(in);
        Utils.closeQuietly(out);
    }
}
