import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material';

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
  sourceDb: string;
  targetDb: string;
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
      this.snackBar.open(err.message, 'close');
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
  constructor (private http: HttpClient, private snackBar: MatSnackBar) {
    this.http.get('/rest/info').subscribe((response: any) => {
      this.sourceDb = response.sourceDb;
      this.targetDb = response.targetDb;
    });
    this.updateTasksInfo();
  }
}
