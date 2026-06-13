import fs from 'fs-extra';
import path from 'path';

export async function generateComponent(type, name, options) {
  const projectPath = process.cwd();
  const targetDir = path.join(projectPath, 'src', 'main', 'java', 'mycompany', 'domain');
  
  await fs.ensureDir(targetDir);
  
  if (type === 'service') {
    const serviceName = name.replace(/\b\w/g, l => l.toUpperCase());
    const filePath = path.join(targetDir, `${serviceName}.java`);
    
    const template = `package mycompany.services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;

public class ${serviceName}Service {
    public void get${serviceName}(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        // TODO: Implement service logic
    }
}
`;
    
    await fs.writeFile(filePath, template);
    console.log(`Generated service: ${serviceName}Service`);
  } else if (type === 'domain') {
    const domainName = name.replace(/\b\w/g, l => l.toUpperCase());
    const filePath = path.join(targetDir, `${domainName}.java`);
    
    const template = `package mycompany.domain;

import koo.framework.domain.FrameworkEntity;

public class ${domainName} extends FrameworkEntity {
    private String name;
    private String description;
    
    public ${domainName}() {}
    
    public ${domainName}(String name) {
        this.name = name;
    }
}
`;
    
    await fs.writeFile(filePath, template);
    console.log(`Generated domain class: ${domainName}`);
  }
}