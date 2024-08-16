module io.github.sinri.keel {
    uses io.github.sinri.keel.helper.authenticator.googleauth.async.AsyncICredentialRepository;
    uses io.github.sinri.keel.helper.authenticator.googleauth.sync.ICredentialRepository;

    exports io.github.sinri.keel.cache;

    exports io.github.sinri.keel.core;
    exports io.github.sinri.keel.core.cutter;
    exports io.github.sinri.keel.core.json;

    exports io.github.sinri.keel.elasticsearch;
    exports io.github.sinri.keel.elasticsearch.document;
    exports io.github.sinri.keel.elasticsearch.index;
    exports io.github.sinri.keel.elasticsearch.search;

    exports io.github.sinri.keel.email.smtp;

    exports io.github.sinri.keel.facade;
    exports io.github.sinri.keel.facade.async;
    exports io.github.sinri.keel.facade.cluster;
    exports io.github.sinri.keel.facade.configuration;
    exports io.github.sinri.keel.facade.launcher;

    exports io.github.sinri.keel.helper;
    exports io.github.sinri.keel.helper.authenticator.googleauth;
    exports io.github.sinri.keel.helper.authenticator.googleauth.async;
    exports io.github.sinri.keel.helper.authenticator.googleauth.sync;
    exports io.github.sinri.keel.helper.encryption.rsa;
    exports io.github.sinri.keel.helper.encryption.aes;
    exports io.github.sinri.keel.helper.runtime;

    exports io.github.sinri.keel.logger;
    exports io.github.sinri.keel.logger.event;
    exports io.github.sinri.keel.logger.issue.center;
    exports io.github.sinri.keel.logger.issue.record;
    exports io.github.sinri.keel.logger.issue.recorder;
    exports io.github.sinri.keel.logger.issue.recorder.adapter;
    exports io.github.sinri.keel.logger.issue.recorder.render;
    exports io.github.sinri.keel.logger.metric;

    exports io.github.sinri.keel.maids.gatling;
    exports io.github.sinri.keel.maids.watchman;

    exports io.github.sinri.keel.markdown;

    exports io.github.sinri.keel.mysql;
    exports io.github.sinri.keel.mysql.action;
    exports io.github.sinri.keel.mysql.condition;
    exports io.github.sinri.keel.mysql.dev;
    exports io.github.sinri.keel.mysql.exception;
    exports io.github.sinri.keel.mysql.matrix;
    exports io.github.sinri.keel.mysql.statement;
    exports io.github.sinri.keel.mysql.statement.component;
    exports io.github.sinri.keel.mysql.statement.templated;

    exports io.github.sinri.keel.poi.csv;
    exports io.github.sinri.keel.poi.excel;
    exports io.github.sinri.keel.poi.excel.reader;
    exports io.github.sinri.keel.poi.excel.reader.options;
    exports io.github.sinri.keel.poi.excel.reader.entity;
    exports io.github.sinri.keel.poi.excel.writer;
    exports io.github.sinri.keel.cache.temporaryvalue;
    exports io.github.sinri.keel.cache.impl;

    exports io.github.sinri.keel.redis;
    exports io.github.sinri.keel.redis.mixin;

    exports io.github.sinri.keel.servant.funnel;
    exports io.github.sinri.keel.servant.intravenous;
    exports io.github.sinri.keel.servant.sundial;
    exports io.github.sinri.keel.servant.queue;

    exports io.github.sinri.keel.tesuto;
    exports io.github.sinri.keel.verticles;

    exports io.github.sinri.keel.web.http;
    exports io.github.sinri.keel.web.http.fastdocs;
    exports io.github.sinri.keel.web.http.prehandler;
    exports io.github.sinri.keel.web.http.receptionist;

    exports io.github.sinri.keel.web.tcp;
    exports io.github.sinri.keel.web.tcp.piece;

    exports io.github.sinri.keel.web.udp;

    //requires jsr305;
    requires org.apache.poi.ooxml;
    requires io.vertx.core;
    requires com.github.pjfanning.excelstreamingreader;
    requires com.hazelcast.core;
    requires io.vertx.clustermanager.hazelcast;
    requires io.vertx.config;
    requires io.vertx.web;
    requires com.github.oshi;
    requires java.management;
    requires io.vertx.auth.common;
    requires java.logging;
    requires io.vertx.client.redis;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
    requires io.vertx.client.sql;
    requires io.vertx.client.sql.mysql;
    requires io.vertx.web.client;
    requires io.vertx.client.mail;
    requires org.apache.commons.collections4;
    requires org.jetbrains.annotations;
}