package mock;

import com.skp.di.rake.client.logger.RakeLogger;
import com.skp.di.rake.client.persistent.RakeDao;

import org.apache.http.HttpResponse;

import java.io.IOException;

public class TestRakeLogger extends RakeLogger {
    public TestRakeLogger(RakeDao dao) {
        super(dao);
    }

    @Override
    protected HttpResponse executePost(String body) throws IOException {
        return MockServer.respond();
    }

    @Override
    protected void handleRakeException(int statusCode, String responseBody) {
        verifyResponse(statusCode, responseBody);
    }
}


