import React from 'react'
import Pagination from "app/shared/Pagination";
import { formatDateTime } from "app/lib/dateUtils";
import { DateTime } from "luxon";
import { Link } from "react-router-dom";


export default function AgendaUpdatesResults({ response, pageParams, onPageChange, showDetail }) {

  if (!response.result || response.result.size === 0) {
    return (
      <div className="text-center">
        No results found
      </div>
    )
  }

  return (
    <div className="mt-8">
      <div className="text-center">
        <span className="font-semibold">{response.total.toLocaleString()}</span>&nbsp;matches found.
      </div>
      <Pagination
        limit={pageParams.limit}
        currentPage={pageParams.selectedPage}
        onPageChange={onPageChange}
        total={response.total} />
      <ResultList results={response.result.items} showDetail={showDetail} />
      <Pagination
        limit={pageParams.limit}
        currentPage={pageParams.selectedPage}
        onPageChange={onPageChange}
        total={response.total} />
    </div>
  )
}

function ResultList({ results, showDetail }) {
  return (
    <div>
      {results.map((r, i) =>
        <ResultItem result={r} showDetail={showDetail} key={i} />
      )}
    </div>
  )
}

function ResultItem({ result, showDetail }) {
  return (
    <div className="mb-8">
      <h4 className="h5">
        {showDetail &&
          <span>{result.action} - {result.scope} - </span>
        }
        <span className="link"><Link to={`/agendas/${result.id.year}/${result.id.number}`}>
          Agenda {result.id.number} ({result.id.year})
        </Link></span>
      </h4>
      <div className="text">
        <div>
          Published Date: {formatDateTime(result.sourceDateTime, DateTime.DATETIME_MED)}
        </div>
        <div>
          Processed Date: {formatDateTime(result.processedDateTime, DateTime.DATETIME_MED)}
        </div>
        <div>
          Source: <span className="link"><Link to={`/api/3/sources/fragment/${result.sourceId}`}
                                               target="_blank">{result.sourceId}</Link></span>
        </div>
      </div>
      {showDetail &&
        <div className="py-2">
          <FieldTable update={result} />
        </div>
      }

    </div>
  )
}

function FieldTable({ update }) {
  if (!update.fields) {
    return null
  }

  return (
    <table className="table table--stripe">
      <thead>
      <tr>
        <th>Field Name</th>
        <th>Data</th>
      </tr>
      </thead>
      <tbody>
      {Object.entries(update.fields).map(([ key, value ]) => {
        return (
          <tr key={key}>
            <td>{key}</td>
            <td>
              <pre className="whitespace-pre-wrap">{value}</pre>
            </td>
          </tr>
        )
      })}
      </tbody>
    </table>
  )
}
