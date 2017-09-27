package ai.grakn.redismock;

import ai.grakn.redismock.expecptions.EOFException;
import ai.grakn.redismock.expecptions.ParseErrorException;
import com.google.common.base.Preconditions;
;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Xiaolu on 2015/4/18.
 */
public class RedisWorker implements Runnable {

    private final CommandExecutor executor;
    private final Socket socket;
    private final ServiceOptions options;
    private final InputStream in;
    private final OutputStream out;

    public RedisWorker(CommandExecutor executor, Socket socket,  ServiceOptions options) throws IOException {
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(socket);
        Preconditions.checkNotNull(options);

        this.executor = executor;
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
                Slice resp = executor.execCommand(command);
                out.write(resp.data());
                out.flush();
                count++;
                if (options.getCloseSocketAfterSeveralCommands() != 0
                        && options.getCloseSocketAfterSeveralCommands() == count) {
                    break;
                }
            } catch (IOException e) {
                // Do nothing
                break;
            } catch (ParseErrorException e) {
                // TODO return error
            } catch (EOFException e) {
                break;
            }
        }
        Utils.closeQuietly(socket);
        Utils.closeQuietly(in);
        Utils.closeQuietly(out);
    }
}
