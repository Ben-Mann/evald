package net.benmann.evald;

import java.util.List;

abstract class CreateNArgFunctionFn {
    final String token;

    CreateNArgFunctionFn(String token) {
        this.token = token;
    }

    protected abstract ValueNode fn(List<Node> args);
}