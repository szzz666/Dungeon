package top.szzz666.Dungeon.tools;

import cn.nukkit.scheduler.TaskHandler;

public class TaskData {
    public TaskHandler taskHandler;
    public int current;
    public boolean inProgress;

    public TaskData(int i, TaskHandler taskHandler) {
        this.current = i;
        this.taskHandler = taskHandler;
        this.inProgress = true;
    }
    public TaskData() {
        this.inProgress = true;
    }
}
