package com.fzolv.shareware.core.exception;

public class GroupNotFoundException extends ApiException {
    public GroupNotFoundException(String groupId) {
        super("GROUP_NOT_FOUND", 404, "Group not found: " + groupId);
    }
}
