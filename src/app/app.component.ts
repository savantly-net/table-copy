import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material';

export class DataSource {
  url: string;
  username: string;
  password: string;
  driverClassName: string;
}

export class ColumnMapping {
  sourceColumnName: string;
  targetVariableName: string;
  sourceColumnType: string;
}

export class TransferOptions {
  insertStatement: string;
  selectStatement: string;
  mappings: ColumnMapping[] = [];
}

export class Task {
  id: number;
  done: boolean;
  cancelled: boolean;
  completedExceptionally: boolean;
  message: string;
}

export class TasksInfo {
  activeCount: number;
  poolSize: number;
  keepAliveSeconds: number;
  tasks: Task[];
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'app';
  options: TransferOptions = new TransferOptions();
  sourceDb: DataSource = new DataSource();
  targetDb: DataSource = new DataSource();
  sourceUrl: string;
  targetUrl: string;
  tasksInfo: TasksInfo;

  newMapping = new ColumnMapping();

  addNewMapping(item) {
    this.options.mappings.push(Object.assign({}, item));
  }

  removeMapping(item: ColumnMapping) {
    const index = this.options.mappings.indexOf(item);
    this.options.mappings.splice(index, 1);
  }

  execute(options: TransferOptions) {
    this.http.post('/rest/execute', options).subscribe((response: any) => {
      this.snackBar.open(response.message, 'close');
      this.updateTasksInfo();
    }, err => {
      console.error(err);
      this.snackBar.open(err.error.message, 'close');
    });
  }

  updateTasksInfo() {
    this.http.get('/rest/tasks').subscribe((response: TasksInfo) => {
      this.tasksInfo = response;
    });
  }

  clearDoneTasks() {
    this.http.get('/rest/tasks/clear').subscribe((response: any) => {
      this.snackBar.open(response.message, 'close');
      this.updateTasksInfo();
    });
  }

  testDataSource(dataSource: DataSource) {
    this.http.post('/rest/db/test', dataSource).subscribe((response: any) => {
      this.snackBar.open(response.message, 'close');
    }, err => {
      console.error(err);
      this.snackBar.open(err.error.message, 'close');
    });
  }

  flattenDataSourceConfiguration() {
    const settings = {};

    settings['source.datasource.url'] = this.sourceDb.url;
    settings['source.datasource.username'] = this.sourceDb.username;
    settings['source.datasource.password'] = this.sourceDb.password;
    settings['source.datasource.driverClassName'] = this.sourceDb.driverClassName;

    settings['target.datasource.url'] = this.targetDb.url;
    settings['target.datasource.username'] = this.targetDb.username;
    settings['target.datasource.password'] = this.targetDb.password;
    settings['target.datasource.driverClassName'] = this.targetDb.driverClassName;

    return settings;
  }

  applyDataSourceConfiguration() {
    const settings = this.flattenDataSourceConfiguration();
    this.http.post('/rest/db/', settings).subscribe((response: any) => {
      this.snackBar.open(response.message, 'close');
    }, err => {
      console.error(err);
      this.snackBar.open(err.error.message, 'close');
    });
  }

  constructor (private http: HttpClient, private snackBar: MatSnackBar) {
    this.http.get('/rest/info').subscribe((response: any) => {
      this.sourceUrl = response.sourceDb;
      this.targetUrl = response.targetDb;
    }, err => {
      console.error(err);
      this.snackBar.open(err.error.message, 'close');
    });
    this.updateTasksInfo();
  }
}
