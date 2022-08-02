package com.yaniv.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(
        subcommands = {
                DownloadCommand.class,
                GitStatCommand.class
        }

)
public class GitCommand implements Runnable{

    public static void main(String []args){
        new CommandLine(new GitCommand()).execute(args);
    }
    @Override
    public void run() {
        System.out.println("Git Methods add, commit are expected");
    }
}
