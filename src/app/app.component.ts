import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export class ColumnMapping {
  sourceColumnName: string;
  targetColumnName: string;
  sourceColumnType: string;
}

export class TransferOptions {
    insertStatement: string;
    selectStatement: string;
    mappings: ColumnMapping[] = [];
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

  newMapping = new ColumnMapping();

  addNewMapping(item) {
    this.options.mappings.push(Object.assign({}, item));
  }

  removeMapping(item: ColumnMapping) {
    const index = this.options.mappings.indexOf(item);
    this.options.mappings.splice(index, 1);
  }

  execute(options: TransferOptions) {
    this.http.post('/rest/execute', options).subscribe(response => {

    }, err => {
      console.error(err);
    });
  }

  constructor (private http: HttpClient) {
    this.http.get('/rest/info').subscribe((response: any) => {
      this.sourceDb = response.sourceDb;
      this.targetDb = response.targetDb;
    });
  }
}
