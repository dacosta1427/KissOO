package org.garret.perst.impl;

class ArrayPos {
    byte[] body;
    int    offs;

    ArrayPos(byte[] body, int offs) { 
        this.body = body;
        this.offs = offs;
    }
}