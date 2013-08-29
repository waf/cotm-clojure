import random
import json

class CommandList:
    def __init__(self):
        self.commands = {}
        self.filename = './commands.json'

    def load_commands(self):
        file = open(self.filename, 'rt')
        self.commands = json.load(file)
        file.close()

    def active_commands(self):
        all_true = [key for key, val in self.commands.iteritems() if val]
        return all_true

    def pick_command(self):
        active = self.active_commands()
        random.shuffle(active)
        if active:
            return active.pop()
        else:
            return None

    def mark_inactive(self, command):
        self.commands[command] = False

    def write_commands(self):
        with open(self.filename, 'wt') as file:
            json.dump(self.commands, file, indent=True)
