#!/usr/bin/env node

import { Command } from 'commander';
import { createProject } from './commands/create.js';
import { generateComponent } from './commands/generate.js';
import { build } from './commands/build.js';

const program = new Command();

program
  .name('koo')
  .description('Koo Framework CLI')
  .version('0.1.0');

program
  .command('create <name>')
  .description('Create a new Koo project')
  .option('-t, --template <template>', 'Project template', 'default')
  .action(createProject);

program
  .command('generate <type> <name>')
  .description('Generate a component or service')
  .option('-s, --schema <schema>', 'Proto schema file')
  .action(generateComponent);

program
  .command('build')
  .description('Build the project')
  .option('-e, --env <env>', 'Build environment', 'development')
  .action(build);

program.parse();