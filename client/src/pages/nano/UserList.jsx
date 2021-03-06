import React from 'react'
import styled from 'styled-components'

import Table from '../../components/Table.js'

const Container = styled.div`
  margin: 1rem;
  overflow: auto;

  & > .title {
    position: sticky;
    left: 0;
    margin: .25rem 0;
    font-size: 1.25rem;
    line-height: 1.75rem;
    font-weight: 500;
  }
`

export default function UserList(props) {

    return (
        <Container>
            <div className="title">用户列表</div>
            <Table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>名字</th>
                </tr>
                </thead>
                <tbody>
                {props.list.map(it => (
                    <tr key={it.id}>
                        <td>{it.id}</td>
                        <td style={{ textAlign: 'center' }}>{it.username}</td>
                        <td style={{ textAlign: 'center' }}>{it.firstname}</td>
                    </tr>
                ))}
                </tbody>
            </Table>
        </Container>
    )
}