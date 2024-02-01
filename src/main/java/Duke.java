import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Duke {
    private static ArrayList<Task> taskList = new ArrayList<>();
    public static final String LINE = "    ____________________________________________________________";
    public static void main(String[] args) throws EmptyTaskNameException, NoTaskTypeException, IOException {
        Scanner sc = new Scanner(System.in);

        System.out.printf(
                "%s\n     Hello! I'm Buto\n     What can I do for you?\n%s\n",
                LINE, LINE
        );
        loadFile();

        String command = sc.next();

        while (!command.equals("bye")) {
            switch (command) {
            case "list" :
                printTaskList();
                break;
            case "todo" :
                try {
                    String todoName = sc.nextLine();
                    checkEmptyTask(todoName);
                    addTask(new ToDo(todoName.trim(), false));
                } catch (EmptyTaskNameException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "deadline" :
                try {
                    String[] splitDeadline = sc.nextLine().split(" /by ");
                    checkEmptyTask(splitDeadline[0]);
                    String[] splitDateTime = splitDeadline[1].trim().split(" ");
                    if (splitDateTime.length == 2) {
                        LocalDateTime time = LocalDateTime.parse(
                                splitDeadline[1],
                                DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm"));
                        addTask(new Deadline(splitDeadline[0].trim(), false, time));
                    } else {
                        LocalDate time = LocalDate.parse(
                                splitDeadline[1], DateTimeFormatter.ISO_LOCAL_DATE);
                        addTask(new Deadline(splitDeadline[0].trim(), false, time));
                    }
                } catch (EmptyTaskNameException e) {
                    System.out.println(e.getMessage());
                } catch (DateTimeParseException e) {
                    System.out.println(
                            "Please specify the task's deadline using the format 'yyyy-mm-dd hh:mm' or 'yyyy-mm-dd'");
                }
                break;
            case "event" :
                try {
                    String[] splitName = sc.nextLine().split(" /from ");
                    checkEmptyTask(splitName[0]);
                    String[] startEnd = splitName[1].split(" /to ");
                    addTask(new Event(splitName[0].trim(), false, startEnd[0], startEnd[1]));
                } catch (EmptyTaskNameException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "mark" :
                int markIndex = sc.nextInt() - 1;
                taskList.get(markIndex).mark();
                printResponse("Nice! I've marked this task as done:", markIndex);
                break;
            case "unmark" :
                int unmarkIndex = sc.nextInt() - 1;
                taskList.get(unmarkIndex).unmark();
                printResponse("OK, I've marked this task as not done yet:", unmarkIndex);
                break;
            case "delete" :
                Task removed = taskList.remove(sc.nextInt()-1);
                System.out.println(LINE + "\n     Got it. I've removed this task:\n       " + removed.toString());
                System.out.println("     Now you have " + taskList.size() + " tasks in the list.\n" + LINE);
                break;
            default :
                try {
                    throw new NoTaskTypeException();
                } catch (NoTaskTypeException e) {
                    System.out.println(e.getMessage());
                }
                break;
            }
            command = sc.next();
        }

        sc.close();
        writeFile();
        System.out.printf("%s\n     Bye. Hope to see you again soon!\n%s",
                LINE, LINE);
    }

    public static void loadFile() throws IOException, FileNotFoundException {
        try {
            File f = new File("./src/main/data/tasks.txt");
            Scanner sc = new Scanner(f);
            while (sc.hasNext()) {
                String[] taskDescriptions = sc.nextLine().split(" ");
                String taskName = taskDescriptions[0];
                boolean done = Boolean.parseBoolean(taskDescriptions[1]);
                switch (taskDescriptions.length) {
                    case 2 :
                        taskList.add(new ToDo(taskName, done));
                        break;
                    case 3 :
                        if (taskDescriptions[2].split("T").length > 1) {
                            LocalDateTime time = LocalDateTime.parse(taskDescriptions[2]);
                            taskList.add(new Deadline(taskName, done, time));
                        } else {
                            LocalDate time = LocalDate.parse(taskDescriptions[2]);
                            taskList.add(new Deadline(taskName, done, time));
                        }
                        break;
                    case 4 :
                        taskList.add(new Event(taskName, done, taskDescriptions[2], taskDescriptions[3]));
                        break;
                    default:
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            Files.createDirectories(Paths.get("./src/main/data"));
            System.out.printf(
                    "     You don't have any tasks yet!\n%s\n",
                    LINE
            );
        }
    }
    public static void writeFile() throws IOException {
        File f = new File("./src/main/data/tasks.txt");
        FileWriter writer = new FileWriter(f);
        String text = "";
        for (Task t : taskList) {
            text += t.storeData() + "\n";
        }
        writer.write(text);
        writer.close();
    }
    public static void checkEmptyTask(String taskName) throws EmptyTaskNameException {
        if (taskName.trim().isEmpty()) {
            throw new EmptyTaskNameException();
        }
    }

    public static void addTask(Task newTask) {
        taskList.add(newTask);
        System.out.println(LINE + "\n     Got it. I've added this task:\n       " + newTask.toString());
        System.out.println("     Now you have " + taskList.size() + " tasks in the list.\n" + LINE);
    }

    public static void printTaskList() {
        System.out.println(LINE + "\n     Here are the tasks in your list:");
        for (int i = 1; i <= taskList.size(); i++) {
            System.out.printf("     %d.%s\n", i, taskList.get(i - 1).toString());
        }
        System.out.println(LINE);
    }

    public static void printResponse(String response, int taskIndex) {
        System.out.printf("%s\n     %s\n       %s\n%s\n",
                LINE, response, taskList.get(taskIndex).toString(), LINE);
    }
}