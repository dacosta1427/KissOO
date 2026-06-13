import fs from 'fs-extra';
import path from 'path';

export async function createProject(name, options) {
  const projectPath = process.cwd();
  const targetDir = path.join(projectPath, name);
  
  await fs.ensureDir(targetDir);
  await fs.copy(path.join(process.cwd(), '../templates/default'), targetDir);
  
  console.log(`Created new Koo project: ${name}`);
  console.log(`Project location: ${targetDir}`);
  console.log('\nNext steps:');
  console.log('  cd ' + name);
  console.log('  koo generate service House');
  console.log('  koo build');
}