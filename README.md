# java-http

## Description

A simple java http client abstraction

## Usage

```java
import com.pedrozc90.http.exceptions.HttpResponseException;

public class Main {

    private final HttpClient client;

    public static void main(String[] args) {
        try {
            // create request object
            final Request request = Request.builder()
                .url("http://localhost:9000/path/to/resource")
                .header("header", "value")
                .body("body")
                .build();

            // send request
            final Response response = client.execute(request);

            final byte[] bytes = response.getBody();
            final String payload = response.asString();
            final File file = response.asFile();
            final MyObject object = response.as(MyObject.class);
        } catch (HttpResponseException e) {
            throw new RuntimeException("", e);
        }
    }

}
```

## License

Please read [LICENSE](./LICENSE) file.
