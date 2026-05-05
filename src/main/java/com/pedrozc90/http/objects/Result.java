package com.pedrozc90.http.objects;

import lombok.Data;

@Data
public class Result<T> {

    private final Request<T> request;
    private final Response response;

}
