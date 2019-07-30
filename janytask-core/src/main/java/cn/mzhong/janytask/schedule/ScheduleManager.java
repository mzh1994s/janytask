package cn.mzhong.janytask.schedule;

import cn.mzhong.janytask.application.TaskContext;
import cn.mzhong.janytask.util.PackageUtils;
import cn.mzhong.janytask.worker.TaskExecutor;
import cn.mzhong.janytask.application.TaskManager;
import cn.mzhong.janytask.tool.AnnotationPatternClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.JanyTask$CronSequenceGenerator;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ScheduleManager implements TaskManager {

    Logger Log = LoggerFactory.getLogger(ScheduleManager.class);

    protected String[] packages = new String[0];

    protected TaskContext context;

    protected ScheduleObjectCreator scheduleObjectCreator = new InternalScheduleObjectCreator();

    protected Set<TaskExecutor> executors = new HashSet<TaskExecutor>();

    protected AnnotationPatternClassScanner scanner = new AnnotationPatternClassScanner();

    public void setContext(TaskContext context) {
        this.context = context;
    }

    public String[] getPackages() {
        return packages;
    }

    public void setPackage(String packages) {
        this.packages = PackageUtils.parseMultiple(packages);
    }

    private Set<Class<?>> scanSchedule() throws ClassNotFoundException {
        scanner.addPackages(packages);
        scanner.addAnnotation(Schedule.class);
        scanner.scan();
        return scanner.select();
    }

    private void initSchedule() throws ClassNotFoundException {
        Set<Class<?>> classes = scanSchedule();
        Iterator<Class<?>> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class<?> _class = iterator.next();
            Object schedule = scheduleObjectCreator.createScheduleObject(_class);
            Method[] methods = _class.getMethods();
            int len = methods.length;
            for (int i = 0; i < len; i++) {
                Method method = methods[i];
                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (scheduled != null) {
                    JanyTask$CronSequenceGenerator cronSequenceGenerator = new JanyTask$CronSequenceGenerator(scheduled.cron(), scheduled.zone());
                    executors.add(new ScheduleExecutor(context, schedule, method, cronSequenceGenerator));
                }
            }
        }
    }

    public void init() {
        try {
            initSchedule();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<TaskExecutor> getTaskExecutors() {
        return executors;
    }

    class ScheduleExecutor extends TaskExecutor {

        Object schedule;
        Method scheduled;

        public ScheduleExecutor(TaskContext context, Object schedule, Method scheduled, JanyTask$CronSequenceGenerator cronSequenceGenerator) {
            super(context, cronSequenceGenerator);
            this.schedule = schedule;
            this.scheduled = scheduled;
        }

        @Override
        protected void execute() {
            try {
                scheduled.invoke(schedule);
            } catch (Exception e) {
                Log.error(e.getLocalizedMessage(), e);
            }
        }
    }
}

