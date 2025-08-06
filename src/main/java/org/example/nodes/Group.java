package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class Group extends Node {

    protected final String driving;
    protected final long delayBeforeDriving;
    protected final long delayAfterDriving;

    protected final Set<String> accepts;
    protected final Map<String, Event<?>> state;

    private final List<Previous> previousNodeInputs;
    private final List<Window> windowNodeInputs;

    private Mode mode;

    public Group(String driving, long delayBeforeDriving, long delayAfterDriving, Connection<?>... inputs) {
        this.driving = driving;
        this.delayBeforeDriving = delayBeforeDriving;
        this.delayAfterDriving = delayAfterDriving;

        this.accepts = new HashSet<>();
        this.accepts.add(driving);
        this.accepts.add("SynthInit" + this.hashCode());
        this.accepts.add("SynthEnd" + this.hashCode());
        this.accepts.addAll(Arrays.stream(inputs)
                .filter(c -> c.value() instanceof String)
                .map(c -> (String) c.value())
                .collect(Collectors.toSet()));

        this.state = new HashMap<>();
        this.mode = Mode.None;

        previousNodeInputs = new ArrayList<>();
        windowNodeInputs = new ArrayList<>();

        Arrays.stream(inputs)
                .filter(c -> c.value() instanceof Node)
                .map(c -> (Node) c.value())
                .forEach(
                        node -> {
                            if (node instanceof Previous) {
                                previousNodeInputs.add(((Previous) node).copy());
                            } else if (node instanceof Window) {
                                windowNodeInputs.add(((Window) node).copy());
                            }
                        }
                );
    }

    private static Optional<Event<?>> giveSinglePrev(Previous pr, Event<?> e) {
        if (pr.accepts().contains(e.getName())) {
            pr.lastElem = e;
            return pr.state == null ? Optional.empty() : Optional.of(new Event<>(
                    pr.getOutput(),
                    pr.state.getData(),
                    e.getTimestamp())
            );
        }
        return Optional.empty();
    }

    private static Optional<Event<?>> giveSingleWindow(Group group, Window window, Event<?> e) {

        if (Objects.equals(e.getName(), window.type)) {
            window.buffer.add(e);
            if (group.mode == Mode.None || group.mode == Mode.WaitingOnDrive) {
                group.mode = Mode.WaitingOnDrive;
                Main.addEvent(new Event<>(
                        "SynthInit" + group.hashCode(),
                        new Object[]{window.getOutput(), e.getTimestamp()},
                        e.getTimestamp() + group.delayBeforeDriving
                ));
            }
            window.countdown--;
        }

        if (Objects.equals(window.sizeMode, Window.TIME)) {
            window.buffer.removeIf(event -> event.getTimestamp() + window.size < e.getTimestamp());
        } else if (Objects.equals(window.sizeMode, Window.ITEM)) {
            while (window.buffer.size() >= window.size) {
                window.buffer.remove();
            }
        }

        var output = new Event<?>[0];

        if (e.getName().equals("SynthEnd" + group.hashCode())) {
            output = window.buffer.toArray(Event<?>[]::new);
            window.buffer.clear();
        } else if (e.getName().equals("SynthInit" + group.hashCode())
                && !group.state.containsKey(group.driving)
                && e.getData() instanceof Object[]
                && ((Object[]) e.getData())[0].equals(window.getOutput())) {
            window.buffer.removeIf(event -> ((Object[]) e.getData())[1].equals(event.getTimestamp()));
        }

        if (output.length > 0 && window.accepts().contains(e.getName())) {
            return Optional.of(new Event<>(window.getOutput(),
                    Arrays.stream(output).map(Event::getData).toArray(),
                    e.getTimestamp()));
        }
        return Optional.empty();
    }

    public void init() {
        var group = this;

        previousNodeInputs.forEach(p -> {
            p.inject(e -> giveSinglePrev(p, e));
            Main.addNode(p);
            accepts.add(p.getOutput());
        });

        windowNodeInputs.forEach(w -> {
            w.inject(e -> giveSingleWindow(group, w, e));
            w.synthSignalName = "SynthEnd" + this.hashCode();
            Main.addNode(w);
            accepts.add(w.getOutput());
        });
    }

    @Override
    public Set<String> accepts() {
        return accepts;
    }

    @Override
    public Set<String> requires() {
        return accepts().stream()
                .filter(t -> !t.contains("Synth"))
                .collect(Collectors.toSet());
    }

    public Optional<Event<?>> giveSingle(Event<?> e) {
        if (accepts.contains(e.getName())
                && !e.getName().equals(driving)
                && !e.getName().contains("Synth")
        ) {
            if (mode == Mode.None) {

                state.put(e.getName(), e);
                mode = Mode.WaitingOnDrive;

                if (windowNodeInputs.stream()
                        .noneMatch(w -> Objects.equals(w.getOutput(), e.getName()))
                ) {
                    Main.addEvent(new Event<>(
                            "SynthInit" + this.hashCode(),
                            e.getName(),
                            e.getTimestamp() + this.delayBeforeDriving)
                    );
                }
            } else if (mode == Mode.WaitingOnDrive
                    || mode == Mode.WaitingOnNext
            ) {
                state.put(e.getName(), e);

                if (windowNodeInputs.stream()
                        .noneMatch(w -> Objects.equals(w.getOutput(), e.getName()))
                        && previousNodeInputs.stream()
                        .noneMatch(p -> Objects.equals(p.getOutput(), e.getName()))
                ) {
                    Main.addEvent(new Event<>(
                            "SynthInit" + this.hashCode(),
                            e.getName(),
                            e.getTimestamp() + this.delayBeforeDriving)
                    );
                }
            }
        } else if (e.getName().equals(this.driving)) {
            this.state.put(e.getName(), e);
            this.mode = Mode.WaitingOnNext;
            previousNodeInputs.forEach(p -> {
                if (p.lastElem != null && p.lastElem.getTimestamp() < e.getTimestamp() - delayAfterDriving) {
                    p.lastElem = null;
                }
            });
            Main.addEvent(new Event<>(
                    "SynthEnd" + this.hashCode(),
                    null,
                    e.getTimestamp() + this.delayAfterDriving)
            );
        } else if (e.getName().equals("SynthInit" + this.hashCode())) {
            if (mode == Mode.WaitingOnDrive
                    && !state.containsKey(this.driving)
            ) {
                state.remove(state.keySet().stream()
                        .filter(k -> e.getData().equals(k))
                        .findFirst().orElse(null)
                );
                if (state.isEmpty()) {
                    mode = Mode.None;
                }
            } else if (mode == Mode.WaitingOnDrive) {
                mode = Mode.WaitingOnNext;
            }
        } else if (e.getName().equals("SynthEnd" + this.hashCode())) {
            if (mode == Mode.WaitingOnNext) {
                List<Object> data = accepts.stream()
                        .filter(event -> !event.equals("SynthInit" + this.hashCode())
                                && !event.equals("SynthEnd" + this.hashCode())
                                && !event.equals(driving))
                        .map(key -> state.get(key) == null ? "-" : state.get(key).getData())
                        .collect(Collectors.toList());
                data.addFirst(state.get(driving).getData());
                Optional<Event<?>> output = state.isEmpty() ? Optional.empty() :
                        Optional.of(new Event<>(
                                this.getOutput(),
                                data.toArray(),
                                e.getTimestamp()
                        ));
                windowNodeInputs.forEach(w -> w.buffer.clear());
                previousNodeInputs.forEach((previous) -> {
                    previous.state = previous.lastElem;
                    previous.lastElem = null;
                });
                this.mode = Mode.None;
                state.clear();
                return output;
            }
        }
        return Optional.empty();
    }

    public String getOutput() {
        return "Group(" + driving + ", "
                + this.accepts.stream()
                .filter(event -> !event.equals(driving)
                        && !event.equals("SynthInit" + this.hashCode())
                        && !event.equals("SynthEnd" + this.hashCode()))
                .collect(Collectors.joining(", ")) + ")[" + this.hashCode() + "]";
    }

    private enum Mode {
        None, WaitingOnDrive, WaitingOnNext
    }
}