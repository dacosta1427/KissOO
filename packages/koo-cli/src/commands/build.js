import { execSync } from 'child_process';

export async function build(options) {
  const env = options.env || 'development';
  
  console.log(`Building project for ${env}...`);
  
  try {
    execSync('./bld build', { stdio: 'inherit' });
    console.log('Build completed successfully!');
  } catch (error) {
    console.error('Build failed:', error.message);
    process.exit(1);
  }
}