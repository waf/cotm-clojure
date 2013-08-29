import curses
from time import sleep
import random
import pygame
from command_list import CommandList

def fit_string_to_screen(display_string, pos_x, max_x):
    if pos_x + len(display_string) > max_x:
        return max_x - len(display_string) - 1
    else:
        return pos_x


def random_position(max_x, max_y):
    pos_x = random.randint(1, max_x - 1)
    pos_y = random.randint(1, max_y - 2)

    return (pos_x, pos_y)

def animate_selection(my_screen, display_string, pos_x=None, pos_y=None):
    (max_y, max_x) = my_screen.getmaxyx()
    my_screen.clear()
    my_screen.border(0)

    if (pos_x is None or pos_y is None):
        (pos_x, pos_y) = random_position(max_x, max_y)
    pos_x = fit_string_to_screen(display_string, pos_x, max_x)
    my_screen.addstr(pos_y, pos_x, display_string)
    my_screen.refresh()
    return pos_x, pos_y

def find_delta(curr_pos, dest_pos):
    if curr_pos < dest_pos:
        return 1
    if curr_pos > dest_pos:
        return -1
    return 0

def move_command_to_center(my_screen, display_string, command_position):
    (max_y, max_x) = my_screen.getmaxyx()
    (center_y, center_x) = (max_y / 2, max_x / 2)
    center_x_offset = (center_x - (len(display_string) / 2))

    pos_x, pos_y = command_position

    is_centered = False
    while not is_centered:
        delta_x = find_delta(pos_x, center_x_offset)
        delta_y = find_delta(pos_y, center_y)
        if delta_x == 0 and delta_y == 0:
            is_centered = True
        else:
            pos_x = pos_x + delta_x
            pos_y = pos_y + delta_y
            animate_selection(my_screen, display_string, pos_x, pos_y)
        sleep(0.05)


def main():
    #pygame.mixer.init(frequency=44100, size=-16, channels=2, buffer=4096)
    command_list = CommandList()
    command_list.load_commands()

    my_screen = curses.initscr()
    curses.curs_set(0)

    continue_looping = True

    while continue_looping:
        if command_list.pick_command():
            for i in range(1,random.randint(15,20)):
                display_string = command_list.pick_command()
                command_position = animate_selection(my_screen, display_string)
                #pygame.mixer.Sound('beep.wav').play()
                sleep(i * 0.05)

            move_command_to_center(my_screen, display_string, command_position)

            for i in range(0,3):
                curses.flash()
                sleep(0.2)

            command_list.mark_inactive(display_string)
            command_list.write_commands()
        else:
            continue_looping = False

        char = my_screen.getkey()
        if char in 'q':
            continue_looping = False

    curses.endwin()

if __name__ == '__main__':
    main()
