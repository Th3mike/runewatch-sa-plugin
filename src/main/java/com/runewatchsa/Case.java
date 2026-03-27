package com.runewatchsa;

import lombok.Value;
import java.util.List;

@Value
public class Case
{
    private final String name;
    private final String reason;
    private final String evidence;
    private final String value;
    private final String submissionDate;
    private final String approvalDate;
    private final List<String> nameHistory;
}
