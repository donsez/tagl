/*
 * cron4j - A pure Java cron-like scheduler
 * 
 * Copyright (C) 2008 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package example;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

/**
 * The custom TaskCollector used to retrieve the task list. This sample
 * implementation returns always the same task that the scheduler executes once
 * a minute.
 */
public class MyTaskCollector implements TaskCollector {

	public TaskTable getTasks() {
		SchedulingPattern pattern = new SchedulingPattern("* * * * *");
		Task task = new MyTask();
		TaskTable ret = new TaskTable();
		ret.add(pattern, task);
		return ret;
	}

}
