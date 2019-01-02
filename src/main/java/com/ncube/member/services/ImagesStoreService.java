package com.ncube.member.services;

public interface ImagesStoreService<T> {
    String uploadFile(T file, String uploadFileName);
    void deleteFile(String uploadFileName);
}
