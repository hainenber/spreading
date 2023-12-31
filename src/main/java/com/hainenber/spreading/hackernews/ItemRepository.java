package com.hainenber.spreading.hackernews;

import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, String> {}